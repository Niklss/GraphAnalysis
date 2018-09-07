import com.mxgraph.view.mxGraph;


import java.sql.*;
import java.util.ArrayList;

public class DataBaseHelper {
    private Connection c = null;

    DataBaseHelper() {
        connectToDb();
    }

    private void connectToDb() {
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/postgres",
                            "postgres", "123");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }

    public void close() throws SQLException {
        c.close();
    }



    public void createDbLibVert() {
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE LIBRARIESVERT " +
                    "(ID SERIAL PRIMARY KEY      NOT NULL," +
                    " GROUPID           TEXT    NOT NULL, " +
                    " ARTIFACTID           TEXT    NOT NULL, " +
                    " VERSION           TEXT    NOT NULL, " +
                    " SCOPE           TEXT, " +
                    " NAMEPOM           TEXT    NOT NULL, " +
                    " NAMEJAR           TEXT    NOT NULL) ";

            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table created successfully");
    }
    public void deleteDbLibVert() {
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE LIBRARIESVERT";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table deleted successfully");
    }
    private int insertlibver(Main.VertexInfo parent) throws SQLException {
        if (searchIndexInLibVer(parent) == -1) {
            PreparedStatement st = c.prepareStatement("INSERT INTO LIBRARIESVERT (GROUPID,ARTIFACTID,VERSION,SCOPE,NAMEPOM, NAMEJAR) VALUES (?, ?, ?, ?, ?, ?)");
            st.setString(1, parent.groupId);
            st.setString(2, parent.artifact);
            st.setString(3, parent.version);
            st.setObject(4, parent.scope);
            st.setObject(5, parent.namePOM);
            st.setObject(6, parent.nameJAR);
            st.executeUpdate();
            st.close();
            return 1;
        } else {
            return 0;
        }
    }
    public int insertLibVertex(Main.VertexInfo parent) throws SQLException {
        try {
            return insertlibver(parent);
        } catch (Exception e) {
//            createDbLibVert();
//            insertlibver(parent);
            return 0;
        }
    }
    public int searchIndexInLibVer(Main.VertexInfo v) throws SQLException {
        int id = -1;
        Statement stmt = null;
        stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM LIBRARIESVERT;");
        while (rs.next()) {
            if (rs.getString("NAMEPOM").equals(v.namePOM)) {
                id = rs.getInt("ID");
                rs.close();
                stmt.close();
                return id;
            }
        }
        rs.close();
        stmt.close();
        return id;
    }
    public Main.VertexInfo vertexReturning(int vertId) throws SQLException {
        int id = -1;
        Statement stmt = null;
        stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM LIBRARIESVERT;");
        while (rs.next()) {
            if (rs.getInt("ID") == vertId) {
                Main.VertexInfo v = new Main.VertexInfo(rs.getString("GROUPID"), rs.getString("ARTIFACTID"), rs.getString("SCOPE"), rs.getString("VERSION"));
                return v;
            }
        }
        return null;
    }




    public void createDbLibEdges() {
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE LIBRARIESEDGES " +
                    "(ID SERIAL PRIMARY KEY      NOT NULL," +
                    " LIBSOURCEID           INT    NOT NULL, " +
                    " LIBTARGETID           INT    NOT NULL) ";

            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table created successfully");
    }
    public void deleteDbLibEdges() {
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE LIBRARIESEDGES";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table deleted successfully");
    }
    private int insertlibedge(Main.VertexInfo source, Main.VertexInfo target) throws SQLException {
        if (searchIndexInLibEdge(source, target) == -1) {
            PreparedStatement st = c.prepareStatement("INSERT INTO LIBRARIESEDGES (LIBSOURCEID,LIBTARGETID) VALUES (?, ?)");
            st.setInt(1, searchIndexInLibVer(source));
            st.setInt(2, searchIndexInLibVer(target));
            st.executeUpdate();
            st.close();
            return 1;
        } else {
            return 0;
        }
    }
    public int insertLibEdge(Main.VertexInfo source, Main.VertexInfo target) throws SQLException {
        try {
            return insertlibedge(source, target);
        } catch (Exception e) {
//            createDbLibEdges();
//            insertlibedge(source, target);
            return 0;
        }
    }
    public int searchIndexInLibEdge(Main.VertexInfo source, Main.VertexInfo target) throws SQLException {
        int id = -1;
        Statement stmt = null;
        stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM LIBRARIESEDGES;");
        int sourceId = searchIndexInLibVer(source);
        int targetId = searchIndexInLibVer(target);
        while (rs.next()) {
            if (rs.getInt("LIBSOURCEID") == sourceId) {
                if (rs.getInt("LIBTARGETID") == targetId) {
                    id = rs.getInt("ID");
                    rs.close();
                    stmt.close();
                    return id;
                }
            }
        }
        rs.close();
        stmt.close();
        return id;
    }


    public void createDbLibClasses() {
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE LIBCLASSES " +
                    "(ID SERIAL PRIMARY KEY      NOT NULL," +
                    " NAME           TXT    NOT NULL, " +
                    " LIBVERTID           INT    NOT NULL) ";

            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table created successfully");
    }
    public void deleteDbLibClasses() {
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE LIBCLASSES";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table deleted successfully");
    }
    private int insertlibclass(Main.VertexInfo source, String name) throws SQLException {
        if (searchIndexInLibClasses(source, name) == -1) {
            PreparedStatement st = c.prepareStatement("INSERT INTO LIBCLASSES (NAME,LIBVERTID) VALUES (?, ?)");
            st.setString(1, name);
            st.setInt(2, searchIndexInLibVer(source));
            st.executeUpdate();
            st.close();
            return 1;
        } else {
            return 0;
        }
    }
    public int insertLibClass(Main.VertexInfo source, String name) throws SQLException {
        try {
            return insertlibclass(source, name);
        } catch (Exception e) {
//            createDbLibEdges();
//            insertlibedge(source, target);
            return 0;
        }
    }
    public int searchIndexInLibClasses(Main.VertexInfo source, String name) throws SQLException {
        int id = -1;
        Statement stmt = null;
        stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM LIBCLASSES;");
        int parentId = searchIndexInLibVer(source);
        if (parentId == -1){
            return id;
        }
        while (rs.next()) {
            if (rs.getInt("LIBVERTID") == parentId) {
                if (rs.getString("NAME").equals(name)) {
                    id = rs.getInt("ID");
                    rs.close();
                    stmt.close();
                    return id;
                }
            }
        }
        rs.close();
        stmt.close();
        return id;
    }




    public void createDbLibClassesEdges() {
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE LIBCLASSESEDGES " +
                    "(ID SERIAL PRIMARY KEY      NOT NULL," +
                    " LIBVERTSOURCEID           INT    NOT NULL, " +
                    " LIBVERTTARGETID           INT    NOT NULL) ";

            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table created successfully");
    }
    public void deleteDbLibClassesEdges() {
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE LIBCLASSESEDGES";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table deleted successfully");
    }
    private int insertlibclassedge(Main.VertexInfo sourcelib, String sourceclassname, Main.VertexInfo targetlib, String targetclassname) throws SQLException {
        if (searchIndexInLibClassEdges(sourcelib, sourceclassname, targetlib, targetclassname) == -1) {
            PreparedStatement st = c.prepareStatement("INSERT INTO LIBCLASSESEDGES (LIBVERTSOURCEID,LIBVERTTARGETID) VALUES (?, ?)");
            st.setInt(1, searchIndexInLibClasses(targetlib, targetclassname));
            st.setInt(2, searchIndexInLibClasses(sourcelib, sourceclassname));
            st.executeUpdate();
            st.close();
            return 1;
        } else {
            return 0;
        }
    }
    public int insertLibClassEdge(Main.VertexInfo sourcelib, String sourceclassname, Main.VertexInfo targetlib, String targetclassname) throws SQLException {
        try {
            return insertlibclassedge(sourcelib, sourceclassname, targetlib, targetclassname);
        } catch (Exception e) {
//            createDbLibEdges();
//            insertlibedge(source, target);
            return 0;
        }
    }
    public int searchIndexInLibClassEdges(Main.VertexInfo sourcelib, String sourceclassname, Main.VertexInfo targetlib, String targetclassname) throws SQLException {
        int id = -1;
        Statement stmt = null;
        stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM LIBCLASSESEDGES;");
        int sourceId = searchIndexInLibClasses(sourcelib, sourceclassname);
        int targetId = searchIndexInLibClasses(targetlib, targetclassname);
        if (sourceId == -1 || targetId == -1){
            return id;
        }
        while (rs.next()) {
            if (rs.getInt("LIBVERTSOURCEID") == sourceId) {
                if (rs.getInt("LIBVERTTARGETID") == targetId) {
                    id = rs.getInt("ID");
                    rs.close();
                    stmt.close();
                    return id;
                }
            }
        }
        rs.close();
        stmt.close();
        return id;
    }



    public mxGraph returnGraph() throws SQLException {
        mxGraph mxGraph = new mxGraph();
        Statement stmt = null;
        stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM LIBRARIESEDGES;");
        Statement stmtvert = null;
        stmtvert = c.createStatement();
        ResultSet rsvert = stmtvert.executeQuery("SELECT * FROM LIBRARIESVERT;");

        ArrayList<Object> vertices = new ArrayList<>();

        while(rsvert.next()){
            int libid = rsvert.getInt("id");
            Object vert = vertexReturning(libid).namePOM;
            vertices.add(mxGraph.insertVertex(mxGraph.getDefaultParent(), Integer.toString(libid), vert, 10, 10, 200, 200));
        }

        while (rs.next()){
            int sourceId = rs.getInt("LIBSOURCEID");
            int targetId = rs.getInt("LIBTARGETID");
            int edgeId = rs.getInt("ID");

            mxGraph.insertEdge(mxGraph.getDefaultParent(), Integer.toString(edgeId), null, vertices.get(sourceId - 1), vertices.get(targetId - 1));
        }

        return mxGraph;
    }
}