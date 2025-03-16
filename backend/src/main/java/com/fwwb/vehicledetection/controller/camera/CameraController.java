package com.fwwb.vehicledetection.controller.camera;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fwwb.vehicledetection.domain.model.Camera;
import com.fwwb.vehicledetection.service.CameraService;
import com.fwwb.vehicledetection.util.TokenUtil;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/cameras")
public class CameraController {

    @Autowired
    private CameraService cameraService;

    @Autowired
    private TokenUtil tokenUtil;

    public static final Logger LOGGER = Logger.getLogger(CameraController.class.getName());

    static {
        // 加载 OpenCV 库，确保已正确配置 native 库路径
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // 用于存放摄像头采集会话，key 为 cameraId
    private static final Map<Long, CameraCaptureSession> captureSessions = new ConcurrentHashMap<>();

    // ----------------------------- 摄像头基本操作接口 -----------------------------

    /**
     * 分页列出摄像头
     */
    @GetMapping("/{pageNum}")
    public Page<Camera> listCameras(@PathVariable int pageNum) {
        return cameraService.page(new Page<>(pageNum, 10));
    }

    /**
     * 获取指定摄像头信息
     */
    @GetMapping("/{cameraId}/info")
    public Camera getCamera(@PathVariable Long cameraId) {
        return cameraService.getById(cameraId);
    }

    /**
     * 根据 location 字段进行模糊搜索摄像头（分页）
     */
    @GetMapping("/location")
    public Page<Camera> searchCameras(@RequestParam("location") String keyword,
                                      @RequestParam("pageNum") int pageNum) {
        QueryWrapper<Camera> query = new QueryWrapper<>();
        query.like("location", keyword);
        return cameraService.page(new Page<>(pageNum, 10), query);
    }

    /**
     * 更新摄像头信息
     */
    @PutMapping("/{cameraId}")
    public String updateCamera(@PathVariable Long cameraId, @RequestBody Camera camera) {
        camera.setCameraId(cameraId);
        return cameraService.updateById(camera) ? "更新成功" : "更新失败";
    }

    /**
     * 删除摄像头
     */
    @DeleteMapping("/{cameraId}")
    public String deleteCamera(@PathVariable Long cameraId) {
        return cameraService.removeById(cameraId) ? "删除成功" : "删除失败";
    }

    /**
     * 添加摄像头（支持 USB 与 NETWORK 类型）
     */
    @PostMapping
    public String addCamera(@RequestBody Camera camera) {
        boolean result = cameraService.save(camera);
        return result ? "添加成功" : "添加失败";
    }

    // ----------------------------- 新增的摄像头采集接口 -----------------------------

    /**
     * 1. 打开摄像头启动持续采集
     * URL: /api/cameras/{cameraId}/on
     */
    @GetMapping("/{cameraId}/on")
    public String startCamera(@PathVariable Long cameraId, HttpServletResponse response) throws IOException {
        Camera camera = cameraService.getById(cameraId);
        if (camera == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "摄像头不存在");
            return null;
        }
        // 目前仅支持 USB 摄像头
        if (!"USB".equalsIgnoreCase(camera.getSourceType())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "仅支持 USB 摄像头");
            return null;
        }
        int deviceIndex;
        try {
            deviceIndex = Integer.parseInt(String.valueOf(camera.getDeviceId()));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的设备 ID");
            return null;
        }
        // 如果已经存在则直接提示
        if (captureSessions.containsKey(cameraId)) {
            return "摄像头已处于开启状态";
        }

        CameraCaptureSession session = new CameraCaptureSession(cameraId, deviceIndex);
        boolean started = session.start();
        if (!started) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "无法打开摄像头");
            return null;
        }
        captureSessions.put(cameraId, session);
        return "摄像头已开启";
    }

    /**
     * 2. 快照接口：调用 capture.read(frame) 快速获取一帧，
     * 若摄像头未打开，则返回提示信息
     * URL: /api/cameras/{cameraId}/snapshot
     */
    @GetMapping("/{cameraId}/snapshot")
    public void snapshot(@PathVariable Long cameraId, HttpServletResponse response) throws IOException {
        CameraCaptureSession session = captureSessions.get(cameraId);
        if (session == null || !session.isRunning()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "摄像头未开启，请先调用 /on 接口");
            return;
        }
        // 这里做一个同步读取，防止线程竞争问题
        Mat frame = session.snapshotFrame();
        if (frame == null || frame.empty()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "无法读取摄像头视频流");
            return;
        }
        BufferedImage bufferedImage = matToBufferedImage(frame);
        response.setContentType("image/jpeg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", baos);
        response.getOutputStream().write(baos.toByteArray());
    }

    /**
     * 3. 流媒体接口：持续读取最新帧，若摄像头未开启则返回提示信息
     * URL: /api/cameras/{cameraId}/stream
     */
    @GetMapping("/{cameraId}/stream")
    public void stream(@PathVariable Long cameraId, HttpServletResponse response) throws IOException {
        CameraCaptureSession session = captureSessions.get(cameraId);
        if (session == null || !session.isRunning()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "摄像头未开启，请先调用 /on 接口");
            return;
        }
        response.setContentType("multipart/x-mixed-replace; boundary=frame");
        ServletOutputStream outputStream = response.getOutputStream();
        try {
            while (true) {
                // 从后台定时更新的缓存中获取最新帧
                Mat latestFrame = session.getLatestFrame();
                if (latestFrame == null || latestFrame.empty()) {
                    Thread.sleep(50);
                    continue;
                }
                BufferedImage bufferedImage = matToBufferedImage(latestFrame);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();

                outputStream.write(("--frame\r\n" +
                        "Content-Type: image/jpeg\r\n" +
                        "Content-Length: " + imageBytes.length + "\r\n\r\n").getBytes());
                outputStream.write(imageBytes);
                outputStream.write("\r\n".getBytes());
                outputStream.flush();
                Thread.sleep(125);  // 控制输出帧率
            }
        } catch (IOException e) {
            LOGGER.warning("MJPEG 流传输异常：" + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 4. 关闭摄像头，释放资源
     * URL: /api/cameras/{cameraId}/off
     */
    @GetMapping("/{cameraId}/off")
    public String stopCamera(@PathVariable Long cameraId, HttpServletResponse response) throws IOException {
        CameraCaptureSession session = captureSessions.remove(cameraId);
        if (session == null || !session.isRunning()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "摄像头未开启");
            return null;
        }
        session.stop();
        return "摄像头已关闭";
    }

    // ----------------------------- 工具方法与内部类 -----------------------------

    /**
     * 工具方法：将 OpenCV Mat 转换成 BufferedImage
     *
     * @param mat OpenCV Mat 对象
     * @return BufferedImage 对象
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        mat.get(0, 0, sourcePixels);
        BufferedImage image;
        if (channels == 3) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }
        byte[] targetPixels = ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
        return image;
    }

    /**
     * 内部类，用于维护一个摄像头的 VideoCapture 采集会话
     */
    private static class CameraCaptureSession {
        private final Long cameraId;
        private final int deviceIndex;
        private VideoCapture capture;
        // 用于缓存最新的帧
        private final AtomicReference<Mat> latestFrame = new AtomicReference<>();
        // 采集线程
        private Thread captureThread;
        // 标记是否运行
        private volatile boolean running = false;
        // 锁对象，用于 snapshot 操作时同步读取，避免读取与后台采集线程冲突
        private final Object lock = new Object();

        public CameraCaptureSession(Long cameraId, int deviceIndex) {
            this.cameraId = cameraId;
            this.deviceIndex = deviceIndex;
        }

        /**
         * 启动摄像头采集
         *
         * @return 是否成功启动
         */
        public boolean start() {
            capture = new VideoCapture(deviceIndex);
            // 可以根据需要设置摄像头参数，例如分辨率、帧率等
            if (!capture.isOpened()) {
                return false;
            }
            running = true;
            captureThread = new Thread(() -> {
                Mat frame = new Mat();
                // 持续读取帧
                while (running && capture.isOpened()) {
                    Mat temp = new Mat();
                    boolean readSuccess;
                    // 对 capture.read() 做同步控制，避免 snapshot 与后台线程同时调用
                    synchronized (lock) {
                        readSuccess = capture.read(temp);
                    }
                    if (readSuccess && !temp.empty()) {
                        // 克隆保存最新帧，防止数据被修改
                        latestFrame.set(temp.clone());
                    }
                    try {
                        Thread.sleep(125);  // 控制采集频率
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                capture.release();
            });
            captureThread.start();
            return true;
        }

        /**
         * 快照操作：同步读取一帧（注意此时可能与后台采集线程存在竞争问题，
         * 但这里通过 lock 同步避免同时调用 read）
         */
        public Mat snapshotFrame() {
            Mat frame = new Mat();
            synchronized (lock) {
                // 此处直接调用 read 读取当前帧
                boolean success = capture.read(frame);
                if (!success) {
                    return null;
                }
            }
            return frame;
        }

        /**
         * 获取最新的帧（由后台线程更新的缓存帧）
         */
        public Mat getLatestFrame() {
            return latestFrame.get();
        }

        /**
         * 关闭采集线程与资源
         */
        public void stop() {
            running = false;
            if (captureThread != null) {
                try {
                    captureThread.join(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (capture != null && capture.isOpened()) {
                capture.release();
            }
        }

        public boolean isRunning() {
            return running;
        }
    }
}