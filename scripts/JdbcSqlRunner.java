import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class JdbcSqlRunner {
    private static final String BASE_URL =
            "jdbc:mysql://localhost:3306/HealthControl?useUnicode=true&characterEncoding=UTF-8&useSSL=false"
                    + "&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&allowMultiQueries=true";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        if (args.length == 1 && "--ping".equals(args[0])) {
            ping();
            return;
        }

        if (args.length == 2 && "--query".equals(args[0])) {
            query(args[1]);
            return;
        }

        if (args.length != 1) {
            System.err.println("Usage:");
            System.err.println("  java JdbcSqlRunner --ping");
            System.err.println("  java JdbcSqlRunner --query <sql>");
            System.err.println("  java JdbcSqlRunner <sql-file>");
            System.exit(1);
        }

        runSqlFile(Path.of(args[0]));
    }

    private static void ping() throws Exception {
        try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM appuser")) {
            rs.next();
            System.out.println("Connected to HealthControl. appuser_count=" + rs.getInt(1));
        }
    }

    private static void runSqlFile(Path sqlFile) throws Exception {
        String sql = Files.readString(sqlFile, StandardCharsets.UTF_8);
        if (!sql.isEmpty() && sql.charAt(0) == '\uFEFF') {
            sql = sql.substring(1);
        }

        try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
             Statement st = conn.createStatement()) {
            boolean hasResult = st.execute(sql);
            int updateCount = st.getUpdateCount();
            System.out.println("Executed SQL file: " + sqlFile.toAbsolutePath());
            System.out.println("Initial hasResult=" + hasResult + ", updateCount=" + updateCount);
        }
    }

    private static void query(String sql) throws Exception {
        try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            var meta = rs.getMetaData();
            int columns = meta.getColumnCount();
            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= columns; i++) {
                    if (i > 1) sb.append(" | ");
                    sb.append(meta.getColumnLabel(i)).append("=").append(rs.getString(i));
                }
                System.out.println(sb);
            }
        }
    }
}
