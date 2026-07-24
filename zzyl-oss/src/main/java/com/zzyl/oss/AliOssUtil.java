package com.zzyl.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

//阿里云存储工具类
@Component
public class AliOssUtil {

    private String key = System.getenv("ALIYUN_OSS_ACCESS_KEY_ID");
    private String secret = System.getenv("ALIYUN_OSS_ACCESS_KEY_SECRET");
    private String endpoint = System.getenv().getOrDefault("ALIYUN_OSS_ENDPOINT", "oss-cn-beijing.aliyuncs.com");
    private String bucket = System.getenv().getOrDefault("ALIYUN_OSS_BUCKET", "your-bucket-name");
    private String url = System.getenv().getOrDefault("ALIYUN_OSS_URL", "https://your-bucket-name.oss-cn-beijing.aliyuncs.com");

    //文件上传
    public String upload(String fileName, InputStream inputStream) {

        //创建客户端
        OSS ossClient = new OSSClientBuilder().build(endpoint, key, secret);

        //设置文件最终的路径和名称
        String objectName = "images/" + new SimpleDateFormat("yyyy/MM/dd").format(new Date())
                + "/" + System.currentTimeMillis() + fileName.substring(fileName.lastIndexOf("."));

        //meta设置请求头,解决访问图片地址直接下载
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(getContentType(fileName.substring(fileName.lastIndexOf("."))));

        //上传
        ossClient.putObject(bucket, objectName, inputStream, meta);

        //关闭客户端
        ossClient.shutdown();

        return url + "/" + objectName;
    }

    //文件后缀处理
    private String getContentType(String FilenameExtension) {
        if (FilenameExtension.equalsIgnoreCase(".bmp")) {
            return "image/bmp";
        }
        if (FilenameExtension.equalsIgnoreCase(".gif")) {
            return "image/gif";
        }
        if (FilenameExtension.equalsIgnoreCase(".jpeg") ||
                FilenameExtension.equalsIgnoreCase(".jpg") ||
                FilenameExtension.equalsIgnoreCase(".png")) {
            return "image/jpg";
        }
        return "image/jpg";
    }
}