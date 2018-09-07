import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.view.mxGraph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.SQLException;
import java.util.Scanner;


public class Main {

    static class VertexInfo {
        String groupId;
        String artifact;
        String version;
        String scope;
        String namePOM;
        String nameJAR;

        public VertexInfo(String groupId, String artifact, String version, String scope) {
            this.groupId = groupId.replace(".", "/");
            this.artifact = artifact;
            this.version = version;
            this.scope = scope;
            this.namePOM = artifact + "-" + version + ".pom";
            this.nameJAR = artifact + "-" + version + ".jar";
        }
    }

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, SQLException {

        DataBaseHelper db = new DataBaseHelper();
//        db.deleteDbLibVert();
//        db.deleteDbLibEdges();
//        db.createDbLibVert();
//        db.createDbLibEdges();

        getGraph();

        mxGraph graphMx = db.returnGraph();
        graphMx.getModel().beginUpdate();
        mxHierarchicalLayout customLayout = new mxHierarchicalLayout(graphMx);
        mxIGraphLayout layout = customLayout;
        layout.execute(graphMx.getDefaultParent());
        BufferedImage image = mxCellRenderer.createBufferedImage(graphMx, null,
                1, Color.WHITE, true, null);

        try {
            ImageIO.write(image, "PNG",
                    new File("graph.png"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public static void getGraph() throws IOException, ParserConfigurationException, SAXException, SQLException {
        Scanner in = new Scanner(new File("Libraries"));
        DataBaseHelper db = new DataBaseHelper();

        String groupId = "";
        String artifact = "";
        String version = "";
        String scope = "";

        while (in.hasNextLine()) {
            String[] info = in.nextLine().split(" ");
            groupId = info[0];
            groupId = groupId.replace(".", "/");
            artifact = info[1];
            version = info[2];
            if (info.length == 4) {
                scope = info[3];
            }

            VertexInfo v = new VertexInfo(groupId, artifact, version, scope);
            db.insertLibVertex(v);

            getMat(v, db);
        }
        db.close();
    }

    private static void getMat(VertexInfo v, DataBaseHelper db) throws ParserConfigurationException, SAXException, IOException {
        String groupId = v.groupId;
        groupId = groupId.replace(".", "/");
        String artifact = v.artifact;
        String version = v.version;
        String namePOM = v.namePOM;
        String nameJAR = v.nameJAR;
        String scope;

        String fullString = "http://central.maven.org/maven2" + "/" + groupId + "/" + artifact + "/" + version + "/" + namePOM;
        String fillString = "http://central.maven.org/maven2" + "/" + groupId + "/" + artifact + "/" + version + "/" + nameJAR;

        try {
            URL website = new URL(fullString);
            URL jar = new URL(fillString);

            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            ReadableByteChannel rbcjar = Channels.newChannel(jar.openStream());

            FileOutputStream fos = new FileOutputStream("File.pom");
            FileOutputStream fosjar = new FileOutputStream("File.jar");

            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fosjar.getChannel().transferFrom(rbcjar, 0, Long.MAX_VALUE);

            File filePom = new File("File.pom");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(filePom);

            try {
//                parsingJars("File.jar");

                Element root = document.getDocumentElement();
                Element message = (Element) root.getElementsByTagName("dependencies").item(0);
                Element dep = null;

                Boolean end = false;
                int ind = 0;


                while (!end) {
                    try {
                        dep = (Element) message.getElementsByTagName("dependency").item(ind);
                        groupId = dep.getElementsByTagName("groupId").item(0).getTextContent();
                        groupId = groupId.replace(".", "/");
                        artifact = dep.getElementsByTagName("artifactId").item(0).getTextContent();
                        version = dep.getElementsByTagName("version").item(0).getTextContent();
                        if (version.contains("$")) {
                            ind++;
                            continue;
                        }
                    } catch (NullPointerException e) {
                        break;
                    }

                    try {
                        scope = dep.getElementsByTagName("scope").item(0).getTextContent();
                    } catch (NullPointerException e) {
                        scope = "";
                    }

                    VertexInfo v1 = new VertexInfo(groupId, artifact, version, scope);
                    if (db.insertLibVertex(v1) == 0) {
                        db.insertLibEdge(v, v1);
                        ind++;
                        continue;
                    }
                    db.insertLibEdge(v, v1);


                    getMat(v1, db);
                    ind++;
                }
            } catch (NullPointerException e) {
                System.out.println("sos");
            }
        } catch (FileNotFoundException e) {
            System.out.println("URL not found");
            return;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    private static void parsingJars(String zipFileName) {
//        byte[] buffer = new byte[1024];
//        final File dstDir = new File("Java Library");
//        dstDir.delete();
//        if (!dstDir.exists()) {
//            dstDir.mkdir();
//        }
//        try {
//            final ZipInputStream zis = new ZipInputStream(
//                    new FileInputStream(zipFileName));
//            ZipEntry ze = zis.getNextEntry();
//            String nextFileName;
//            while (ze != null) {
//                nextFileName = ze.getName();
//                File nextFile = new File("Java Library" + File.separator
//                        + nextFileName);
//                System.out.println("Unzipping: "
//                        + nextFile.getAbsolutePath());
//                if (ze.isDirectory()) {
//                    nextFile.mkdir();
//                } else {
//                    new File(nextFile.getParent()).mkdirs();
//                    try (FileOutputStream fos
//                                 = new FileOutputStream(nextFile)) {
//                        int length;
//                        while ((length = zis.read(buffer)) > 0) {
//                            fos.write(buffer, 0, length);
//                        }
//                    }
//
//                    if (nextFileName.endsWith(".class")) {
//                        DataBaseHelper db = new DataBaseHelper();
//
//                        Lexer lexer = new JavaLexer(CharStreams.fromFileName(nextFileName));
//                        TokenStream tokenStream = new CommonTokenStream(lexer);
//                        JavaParser parser = new JavaParser(tokenStream);
//                        Trees tree = new Trees(parser.getATN());
//                    }
//
//                }
//                ze = zis.getNextEntry();
//            }
//            zis.closeEntry();
//            zis.close();
//        } catch (FileNotFoundException ex) {
//        } catch (IOException ex) {
//        }
//    }
}