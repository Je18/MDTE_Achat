package model;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.FileOutputStream;
import java.io.IOException;

public class FTPService {

    private final String server = "ftpperso.free.fr";
    private final int port = 21; 
    private final String user = "pottarn";
    private final String password = "cydeaxch0";

    public String downloadCSV(String remoteFilePath, String localFilePath) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(server, port);
            boolean login = ftpClient.login(user, password);

            if (!login) {
                return "Connexion FTP �chou�e.";
            }

            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            try (FileOutputStream fos = new FileOutputStream(localFilePath)) {
                boolean success = ftpClient.retrieveFile(remoteFilePath, fos);
                if (success) {
                    return "Fichier t�l�charg� avec succ�s.";
                } else {
                    return "�chec du t�l�chargement.";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Erreur lors du t�l�chargement : " + e.getMessage();
        } finally {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}