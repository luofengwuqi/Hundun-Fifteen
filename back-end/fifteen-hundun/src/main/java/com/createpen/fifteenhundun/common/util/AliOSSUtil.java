package com.createpen.fifteenhundun.common.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.PutObjectRequest;
import com.createpen.fifteenhundun.common.config.AliyunProperties;
import com.createpen.fifteenhundun.common.exception.MyException.OSSException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class AliOSSUtil {

    private final AliyunProperties aliyunProperties;

    public AliOSSUtil(AliyunProperties aliyunProperties) {
        this.aliyunProperties = aliyunProperties;
    }

    /**
     * 上传文件到OSS
     * @param file 前端上传的文件
     * @return 文件访问URL
     */

    public String makeUUIDFileName(MultipartFile file){
        // 1. 生成唯一文件名（避免重复）
        String originalFilename = file.getOriginalFilename();
        String suffix = null;
        if (originalFilename != null) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        // 如：a1b2c3d4-xxxx-xxxx-xxxx-xxxxxxx.png
        return UUID.randomUUID() + suffix;
    }
    public String upload(MultipartFile file, String objectName) throws IOException {

        // 2. 获取文件输入流
        InputStream inputStream = file.getInputStream();

        // 3. 创建OSS客户端并上传文件
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                aliyunProperties.getEndpoint(),
                aliyunProperties.getAccessKeyId(),
                aliyunProperties.getAccessKeySecret()
            );
            // 上传文件
            ossClient.putObject(new PutObjectRequest(aliyunProperties.getBucketName(), objectName, inputStream));
            log.info("文件上传OSS成功：{}", objectName);
            // 4. 返回文件访问URL
            return aliyunProperties.getDomain() + "/" + objectName; // 如：https://xxx.oss-cn-hangzhou.aliyuncs.com/memos/avatars/a1b2c3d4.png
        } catch (Exception e) {
            log.error("文件上传OSS失败", e);
            throw new OSSException("文件上传失败");
        } finally {
            // 关闭OSS客户端和输入流
            if (ossClient != null) {
                ossClient.shutdown();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * 删除OSS文件
     * @param fileUrl 文件访问URL或objectName
     */
    public void delete(String fileUrl) {
        // 解析objectName（去掉域名部分）
        String domain = aliyunProperties.getDomain();
        String objectName = fileUrl;
        if (fileUrl.startsWith(domain + "/")) {
            objectName = fileUrl.replace(domain + "/", "");
        } else if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
            // 如果是完整URL，提取路径部分
            int domainEndIndex = fileUrl.indexOf("/", fileUrl.indexOf("//") + 2);
            if (domainEndIndex != -1) {
                objectName = fileUrl.substring(domainEndIndex + 1);
            }
        }
        
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                aliyunProperties.getEndpoint(),
                aliyunProperties.getAccessKeyId(),
                aliyunProperties.getAccessKeySecret()
            );
            ossClient.deleteObject(aliyunProperties.getBucketName(), objectName);
            log.info("文件删除OSS成功：{}", objectName);
        } catch (Exception e) {
            log.error("文件删除OSS失败", e);
            throw new OSSException("文件删除失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 获取OSS域名
     */
    public String getDomain() {
        return aliyunProperties.getDomain();
    }

    /**
     * 列出指定前缀下的所有文件对象名（支持分页）
     * @param prefix 目录前缀，如：music/audio/
     * @return 文件对象名列表
     */
    public List<String> listObjects(String prefix) {
        OSS ossClient = null;
        List<String> objectNames = new ArrayList<>();
        try {
            ossClient = new OSSClientBuilder().build(
                aliyunProperties.getEndpoint(),
                aliyunProperties.getAccessKeyId(),
                aliyunProperties.getAccessKeySecret()
            );

            String nextMarker = null;
            ObjectListing objectListing;

            do {
                ListObjectsRequest request = new ListObjectsRequest(aliyunProperties.getBucketName())
                        .withPrefix(prefix)
                        .withMarker(nextMarker);

                objectListing = ossClient.listObjects(request);

                for (OSSObjectSummary summary : objectListing.getObjectSummaries()) {
                    objectNames.add(summary.getKey());
                }

                nextMarker = objectListing.getNextMarker();
            } while (objectListing.isTruncated());

            log.info("列出OSS目录 {} 下的文件，共 {} 个", prefix, objectNames.size());
            return objectNames;
        } catch (Exception e) {
            log.error("列出OSS目录 {} 失败", prefix, e);
            throw new OSSException("列出OSS目录失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 删除指定目录下的所有文件
     * @param prefix 目录前缀，如：username/files/
     */
    public void deleteDirectory(String prefix) {
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                aliyunProperties.getEndpoint(),
                aliyunProperties.getAccessKeyId(),
                aliyunProperties.getAccessKeySecret()
            );
            
            // 列出目录下所有文件
            ObjectListing objectListing = ossClient.listObjects(new ListObjectsRequest(aliyunProperties.getBucketName()).withPrefix(prefix));
            List<OSSObjectSummary> objectSummaries = objectListing.getObjectSummaries();
            
            if (objectSummaries.isEmpty()) {
                log.info("目录 {} 下没有文件", prefix);
                return;
            }
            
            log.info("开始删除目录 {} 下的 {} 个文件", prefix, objectSummaries.size());
            
            // 逐个删除文件
            for (OSSObjectSummary objectSummary : objectSummaries) {
                String key = objectSummary.getKey();
                ossClient.deleteObject(aliyunProperties.getBucketName(), key);
                log.info("删除文件：{}", key);
            }
            
            log.info("目录 {} 删除完成", prefix);
        } catch (Exception e) {
            log.error("删除目录 {} 失败", prefix, e);
            throw new OSSException("删除目录失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}