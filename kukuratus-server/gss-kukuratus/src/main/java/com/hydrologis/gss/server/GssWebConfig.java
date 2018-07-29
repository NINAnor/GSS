package com.hydrologis.gss.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;

import org.hortonmachine.gears.utils.files.FileUtilities;

import com.hydrologis.kukuratus.libs.workspace.KukuratusWorkspace;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

public class GssWebConfig {

    @WebServlet("/*")
    @VaadinServletConfiguration(ui = GssApplication.class, productionMode = false)
    public static class WebappVaadinServlet extends VaadinServlet {
    }

    @WebListener
    public static class JdbcExampleContextListener implements ServletContextListener {

        @Override
        public void contextInitialized( ServletContextEvent sce ) {
            /// called when the system starts up and the servlet context is initialized
            try {
                GssDbProvider.INSTANCE.init();

                File dataFolder = KukuratusWorkspace.getInstance().getDataFolder(null).get();
                File notesOutFile = new File(dataFolder, "notes.png");
                File imagesOutFile = new File(dataFolder, "images.png");
                if (!notesOutFile.exists()) {
                    InputStream notesIs = GssWebConfig.class.getResourceAsStream("/images/notes.png");
                    Files.copy(notesIs, notesOutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                if (!imagesOutFile.exists()) {
                    InputStream imagesIs = GssWebConfig.class.getResourceAsStream("/images/images.png");
                    Files.copy(imagesIs, imagesOutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void contextDestroyed( ServletContextEvent sce ) {
            try {
                GssDbProvider.INSTANCE.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}