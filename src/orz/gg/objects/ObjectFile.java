package orz.gg.objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 *
 * @author GustavoG
 */
public class ObjectFile {

    private String name;
    private String absolutePath;
    private boolean exist;
    private boolean directory;
    private boolean file;
    private boolean hidden;
    private long lastModified;
    private long length;
    private File fileObject;
    private SmbFile smbFileObject;
    private InputStream inputStream;
    private OutputStream outputStream;

    public ObjectFile(String path) {
        this(path, null, null);
    }

    public ObjectFile(String path, String user, String pass) {
        if (path.startsWith("smb")) {
            validarRuta(path, user, pass);
        } else if ((path.startsWith("//")) || (path.startsWith("\\\\"))) {
            validarRuta("smb:" + path, user, pass);
        } else {
            String sSistemaOperativo = System.getProperty("os.name");
            if (sSistemaOperativo.contains("Linux")) {
                path = path.replace("\\", "/");
                path = path.replace(" ", "%20");
            }
            file(new File(path));
        }

    }

    public ObjectFile(File f) {
        file(f);
    }

    public ObjectFile(SmbFile f) {
        smb(f);
    }

    private void validarRuta(String path, String user, String pass) {
        String sSistemaOperativo = System.getProperty("os.name");
        if (sSistemaOperativo.contains("Linux")) {
            path = path.replace("\\", "/");
            path = path.replace(" ", "%20");
            if ((user == null) || (pass == null)) {
                try {
                    smb(new SmbFile(path));
                } catch (MalformedURLException ex) {
                    JOptionPane.showMessageDialog(null, "ha ocurrido un error al intentar acceder a la ruta del repositio por favor rectificar existencia y/o datos del usuario");
                }
            } else {
                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("", user, pass);
                try {
                    smb(new SmbFile(path, auth));
                } catch (MalformedURLException ex) {
                    JOptionPane.showMessageDialog(null, "ha ocurrido un error al intentar acceder a la ruta del repositio por favor rectificar existencia y/o datos del usuario");
                }
            }
        } else {
            path = path.replace("/", "\\");
            path = path.replace("%20", " ");
            if (path.startsWith("smb:")) {
                path = path.substring(4);
            }
            if ((user == null) || (pass == null)) {
                file(new File(path));
            } else {
                try {
                    Runtime.getRuntime().exec("cmd /c net use \"" + path + "\" " + pass + " /user:" + user);
                    file(new File(path));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "ha ocurrido un error al intentar acceder a la ruta del repositio por favor rectificar existencia y/o datos del usuario");
                }
            }
        }
    }

    private void smb(SmbFile f) {
        try {
            this.smbFileObject = f;
            this.name = f.getName();
            this.absolutePath = f.getCanonicalPath();
            this.exist = f.exists();
            this.directory = f.isDirectory();
            this.file = f.isFile();
            this.hidden = f.isHidden();
            this.lastModified = f.lastModified();
            if (f.isFile()) {
                this.length = f.length();
            }
        } catch (SmbException ex) {
            Logger.getLogger(ObjectFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void file(File f) {
        this.fileObject = f;
        this.name = f.getName();
        this.absolutePath = f.getAbsolutePath();
        this.exist = f.exists();
        this.directory = f.isDirectory();
        this.file = f.isFile();
        this.hidden = f.isHidden();
        this.lastModified = f.lastModified();
        if (f.isFile()) {
            this.length = f.length();
        }
    }

    public long length() {
        return length;
    }

    public String getName() {
        return name;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public long lastModified() {
        return lastModified;
    }

    public boolean exists() {
        return exist;
    }

    public boolean isDirectory() {
        return directory;
    }

    public boolean isFile() {
        return file;
    }

    public boolean isHidden() {
        return hidden;
    }

    public InputStream getInputStream() throws FileNotFoundException {
        if (fileObject != null) {
            return new FileInputStream(fileObject);
        } else {
            try {
                return smbFileObject.getInputStream();
            } catch (SmbException ex) {
                Logger.getLogger(ObjectFile.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ObjectFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return null;
    }

    public OutputStream getOutputStream() throws FileNotFoundException {
        if (fileObject != null) {
            return new FileOutputStream(fileObject);
        } else {
            try {
                return smbFileObject.getOutputStream();
            } catch (SmbException ex) {
                Logger.getLogger(ObjectFile.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ObjectFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public void mkdirs() {
        if (fileObject != null) {
            fileObject.mkdirs();
        } else {
            try {
                smbFileObject.mkdirs();
            } catch (SmbException ex) {
                Logger.getLogger(ObjectFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public ObjectFile[] listFiles() {
        ObjectFile[] listfile = null;
        if (fileObject != null) {
            File[] f = fileObject.listFiles();
            listfile = new ObjectFile[f.length];
            for (int i = 0; i < f.length; i++) {
                listfile[i] = new ObjectFile(f[i]);
            }
        } else {
            try {
                SmbFile[] f = smbFileObject.listFiles();
                listfile = new ObjectFile[f.length];
                for (int i = 0; i < f.length; i++) {
                    listfile[i] = new ObjectFile(f[i]);
                }
            } catch (SmbException ex) {
                Logger.getLogger(ObjectFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return listfile;
    }
    
    public void createNewFile() throws IOException{
        if (fileObject != null) {
            fileObject.createNewFile();
        } else {
            try {
                smbFileObject.createNewFile();
            } catch (SmbException ex) {
                Logger.getLogger(ObjectFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
