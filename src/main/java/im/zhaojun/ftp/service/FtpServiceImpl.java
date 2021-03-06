package im.zhaojun.ftp.service;

import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.ftp.Ftp;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.StorageConfigService;
import im.zhaojun.common.util.StringUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FtpServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FtpServiceImpl.class);

    @Resource
    private StorageConfigService storageConfigService;

    private static final String HOST_KEY = "host";

    private static final String PORT_KEY = "port";

    private static final String USERNAME_KEY = "username";

    private static final String PASSWORD_KEY = "password";

    private static final String DOMAIN_KEY = "domain";

    private Ftp ftp;

    private String domain;

    private boolean isInitialized;

    @Override
    public void init() {
       try {
           Map<String, StorageConfig> stringStorageConfigMap =
                   storageConfigService.selectStorageConfigMapByKey(StorageTypeEnum.FTP);
           String host = stringStorageConfigMap.get(HOST_KEY).getValue();
           String port = stringStorageConfigMap.get(PORT_KEY).getValue();
           String username = stringStorageConfigMap.get(USERNAME_KEY).getValue();
           String password = stringStorageConfigMap.get(PASSWORD_KEY).getValue();
           domain = stringStorageConfigMap.get(DOMAIN_KEY).getValue();

           ftp = new Ftp(host, Integer.parseInt(port), username, password);
           isInitialized = true;
       } catch (Exception e) {
           log.debug(StorageTypeEnum.FTP.getDescription() + "初始化异常, 已跳过");
       }
    }

    @Override
    public List<FileItemDTO> fileList(String path) {
        FTPFile[] ftpFiles = ftp.lsFiles(path);

        List<FileItemDTO> fileItemList = new ArrayList<>();

        for (FTPFile ftpFile : ftpFiles) {
            FileItemDTO fileItemDTO = new FileItemDTO();
            fileItemDTO.setName(ftpFile.getName());
            fileItemDTO.setSize(ftpFile.getSize());
            fileItemDTO.setTime(ftpFile.getTimestamp().getTime());
            fileItemDTO.setType(ftpFile.isDirectory() ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
            fileItemDTO.setPath(path);
            if (ftpFile.isFile()) {
                fileItemDTO.setUrl(getDownloadUrl(StringUtils.concatUrl(path, fileItemDTO.getName())));
            }
            fileItemList.add(fileItemDTO);
        }
        return fileItemList;
    }

    @Override
    public String getDownloadUrl(String path) {
        return URLUtil.complateUrl(domain, path);
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.FTP;
    }


    @Override
    public boolean getIsInitialized() {
        return isInitialized;
    }
}
