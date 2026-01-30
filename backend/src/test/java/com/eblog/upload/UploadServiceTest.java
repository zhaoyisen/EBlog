package com.eblog.upload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.eblog.api.common.ErrorCode;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

  @Mock
  private MinioClient minioClient;

  private UploadService uploadService;

  @BeforeEach
  void setup() {
    uploadService = new UploadService(minioClient, "eblog-public", "http://minio:9000", 1024);
  }

  @Test
  void rejectsTooLarge() {
    UploadService.PresignResult res = uploadService.presignPublicUpload(
        1L, "a.png", "image/png", 2048L);
    assertEquals(false, res.isSuccess());
    assertEquals(ErrorCode.BAD_REQUEST, res.getError());
  }

  @Test
  void rejectsUnsupportedType() {
    UploadService.PresignResult res = uploadService.presignPublicUpload(
        1L, "a.exe", "application/x-msdownload", 10L);
    assertEquals(false, res.isSuccess());
    assertEquals(ErrorCode.BAD_REQUEST, res.getError());
  }

  @Test
  void returnsPresignedUrlAndPublicUrl() throws Exception {
    when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://signed");

    UploadService.PresignResult res = uploadService.presignPublicUpload(
        7L, "hello world.png", "image/png", 10L);

    assertEquals(true, res.isSuccess());
    assertNotNull(res.getData());
    assertEquals("http://signed", res.getData().uploadUrl);
    assertNotNull(res.getData().publicUrl);
    assertEquals("eblog-public", res.getData().bucket);
  }
}
