import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExportFoodDbSnapshot {
    private static final String BASE_URL =
            "jdbc:mysql://localhost:3306/HealthControl?useUnicode=true&characterEncoding=UTF-8&useSSL=false"
                    + "&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    private record CategoryRow(
            int categoryId,
            String categoryName,
            String categoryTypeLabel,
            String categoryDesc,
            int foodCount) {}

    private record FoodRow(
            int id,
            int categoryId,
            String categoryName,
            String categoryTypeLabel,
            String foodName,
            String foodAlias,
            int friendlyLevel,
            String tasteTag,
            String suitPeople,
            String avoidPeople,
            String effectHarmDesc,
            String suggestContent,
            String picType,
            String picUrl) {}

    private record CountRow(String name, int count) {}

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        Path outputPath = args.length > 0
                ? Path.of(args[0]).toAbsolutePath()
                : Path.of("food-db-snapshot.md").toAbsolutePath();

        try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD)) {
            List<CategoryRow> categories = loadCategories(conn);
            List<FoodRow> foods = loadFoods(conn);
            List<CountRow> picSummary = loadPicSummary(conn);
            List<CountRow> levelSummary = loadLevelSummary(conn);

            String markdown = buildMarkdown(categories, foods, picSummary, levelSummary);
            writeUtf8Bom(outputPath, markdown);
            System.out.println("Generated " + outputPath);
        }
    }

    private static List<CategoryRow> loadCategories(Connection conn) throws Exception {
        String sql = """
                select
                  c.Id as CategoryId,
                  c.CategoryName,
                  case when c.CategoryType = 0 then 'friendly' else 'caution' end as CategoryTypeLabel,
                  c.CategoryDesc,
                  count(f.Id) as FoodCount
                from tlaryngealfoodcategory c
                left join tlaryngealfood f
                  on f.CategoryId = c.Id
                 and ifnull(f.IsDelete, 0) = 0
                group by c.Id, c.CategoryName, c.CategoryType, c.CategoryDesc
                order by c.CategoryType, c.Id
                """;

        List<CategoryRow> rows = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new CategoryRow(
                        rs.getInt("CategoryId"),
                        rs.getString("CategoryName"),
                        rs.getString("CategoryTypeLabel"),
                        rs.getString("CategoryDesc"),
                        rs.getInt("FoodCount")));
            }
        }
        return rows;
    }

    private static List<FoodRow> loadFoods(Connection conn) throws Exception {
        String sql = """
                select
                  f.Id,
                  c.Id as CategoryId,
                  c.CategoryName,
                  case when c.CategoryType = 0 then 'friendly' else 'caution' end as CategoryTypeLabel,
                  f.FoodName,
                  f.FoodAlias,
                  f.FriendlyLevel,
                  f.TasteTag,
                  f.SuitPeople,
                  f.AvoidPeople,
                  f.EffectHarmDesc,
                  f.SuggestContent,
                  case
                    when f.PicUrl like 'http://localhost:7245/test-fixtures/foods/%' then 'test-fixtures/foods'
                    when f.PicUrl like '/static/food/%' then '/static/food'
                    when f.PicUrl is null or f.PicUrl = '' then 'empty'
                    else 'other'
                  end as PicType,
                  f.PicUrl
                from tlaryngealfood f
                left join tlaryngealfoodcategory c on c.Id = f.CategoryId
                where ifnull(f.IsDelete, 0) = 0
                order by c.CategoryType, c.Id, f.Id
                """;

        List<FoodRow> rows = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new FoodRow(
                        rs.getInt("Id"),
                        rs.getInt("CategoryId"),
                        rs.getString("CategoryName"),
                        rs.getString("CategoryTypeLabel"),
                        rs.getString("FoodName"),
                        rs.getString("FoodAlias"),
                        rs.getInt("FriendlyLevel"),
                        rs.getString("TasteTag"),
                        rs.getString("SuitPeople"),
                        rs.getString("AvoidPeople"),
                        rs.getString("EffectHarmDesc"),
                        rs.getString("SuggestContent"),
                        rs.getString("PicType"),
                        rs.getString("PicUrl")));
            }
        }
        return rows;
    }

    private static List<CountRow> loadPicSummary(Connection conn) throws Exception {
        String sql = """
                select
                  case
                    when PicUrl like 'http://localhost:7245/test-fixtures/foods/%' then 'test-fixtures/foods'
                    when PicUrl like '/static/food/%' then '/static/food'
                    when PicUrl is null or PicUrl = '' then 'empty'
                    else 'other'
                  end as PicType,
                  count(*) as CountValue
                from tlaryngealfood
                where ifnull(IsDelete, 0) = 0
                group by PicType
                order by CountValue desc, PicType
                """;

        List<CountRow> rows = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new CountRow(rs.getString("PicType"), rs.getInt("CountValue")));
            }
        }
        return rows;
    }

    private static List<CountRow> loadLevelSummary(Connection conn) throws Exception {
        String sql = """
                select FriendlyLevel, count(*) as CountValue
                from tlaryngealfood
                where ifnull(IsDelete, 0) = 0
                group by FriendlyLevel
                order by FriendlyLevel
                """;

        List<CountRow> rows = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new CountRow(rs.getString("FriendlyLevel"), rs.getInt("CountValue")));
            }
        }
        return rows;
    }

    private static String buildMarkdown(
            List<CategoryRow> categories,
            List<FoodRow> foods,
            List<CountRow> picSummary,
            List<CountRow> levelSummary) {

        int friendlyCategoryCount = 0;
        int cautionCategoryCount = 0;
        for (CategoryRow category : categories) {
            if ("friendly".equals(category.categoryTypeLabel())) {
                friendlyCategoryCount++;
            } else if ("caution".equals(category.categoryTypeLabel())) {
                cautionCategoryCount++;
            }
        }

        Map<String, Integer> picCountMap = new LinkedHashMap<>();
        for (CountRow row : picSummary) {
            picCountMap.put(row.name(), row.count());
        }

        StringBuilder sb = new StringBuilder();
        line(sb, "# Food DB Snapshot");
        line(sb);
        line(sb, "- GeneratedAt: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        line(sb, "- Source: local MySQL database HealthControl, tables tlaryngealfood and tlaryngealfoodcategory");
        line(sb, "- Note: this file is exported from the live database, not from healthcontrol.sql");
        line(sb);
        line(sb, "## Overview");
        line(sb);
        line(sb, "| Metric | Value |");
        line(sb, "| --- | --- |");
        line(sb, "| Active food rows | " + foods.size() + " |");
        line(sb, "| Friendly categories | " + friendlyCategoryCount + " |");
        line(sb, "| Caution categories | " + cautionCategoryCount + " |");
        line(sb, "| Images on test-fixtures/foods | " + picCountMap.getOrDefault("test-fixtures/foods", 0) + " |");
        line(sb, "| Images still on /static/food | " + picCountMap.getOrDefault("/static/food", 0) + " |");
        line(sb);
        line(sb, "## Category Summary");
        line(sb);
        line(sb, "| CategoryId | Type | CategoryName | Count | CategoryDesc |");
        line(sb, "| --- | --- | --- | --- | --- |");
        for (CategoryRow category : categories) {
            line(sb, "| " + category.categoryId() + " | " + safe(category.categoryTypeLabel()) + " | "
                    + safe(category.categoryName()) + " | " + category.foodCount() + " | "
                    + safe(category.categoryDesc()) + " |");
        }
        line(sb);
        line(sb, "## Image Summary");
        line(sb);
        line(sb, "| PicType | Count |");
        line(sb, "| --- | --- |");
        for (CountRow row : picSummary) {
            line(sb, "| " + safe(row.name()) + " | " + row.count() + " |");
        }
        line(sb);
        line(sb, "## FriendlyLevel Summary");
        line(sb);
        line(sb, "| FriendlyLevel | Count | Meaning |");
        line(sb, "| --- | --- | --- |");
        for (CountRow row : levelSummary) {
            line(sb, "| " + safe(row.name()) + " | " + row.count() + " | " + friendlyLevelMeaning(row.name()) + " |");
        }
        line(sb);
        line(sb, "## Details");
        line(sb);

        for (CategoryRow category : categories) {
            line(sb, "### " + category.categoryTypeLabel() + " - " + safe(category.categoryName())
                    + " (" + category.categoryId() + ")");
            line(sb);
            line(sb, "| Id | FoodName | FoodAlias | FriendlyLevel | TasteTag | PicType |");
            line(sb, "| --- | --- | --- | --- | --- | --- |");
            for (FoodRow food : foods) {
                if (food.categoryId() != category.categoryId()) {
                    continue;
                }
                line(sb, "| " + food.id() + " | " + safe(food.foodName()) + " | " + safe(food.foodAlias()) + " | "
                        + food.friendlyLevel() + " | " + safe(food.tasteTag()) + " | " + safe(food.picType()) + " |");
            }
            line(sb);
            for (FoodRow food : foods) {
                if (food.categoryId() != category.categoryId()) {
                    continue;
                }
                line(sb, "- [" + food.id() + "] " + safe(food.foodName())
                        + " | SuitPeople=" + safe(food.suitPeople())
                        + " | AvoidPeople=" + safe(food.avoidPeople())
                        + " | EffectHarmDesc=" + safe(food.effectHarmDesc())
                        + " | SuggestContent=" + safe(food.suggestContent())
                        + " | PicUrl=" + safe(food.picUrl()));
            }
            line(sb);
        }

        return sb.toString();
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("|", "\\|");
    }

    private static String friendlyLevelMeaning(String value) {
        return switch (value) {
            case "1" -> "very friendly";
            case "2" -> "friendly";
            case "3" -> "neutral";
            case "4" -> "mildly irritating";
            case "5" -> "strongly irritating";
            default -> "unknown";
        };
    }

    private static void line(StringBuilder sb, String value) {
        sb.append(value).append(System.lineSeparator());
    }

    private static void line(StringBuilder sb) {
        sb.append(System.lineSeparator());
    }

    private static void writeUtf8Bom(Path path, String content) throws Exception {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        try (OutputStream outputStream = Files.newOutputStream(path)) {
            outputStream.write(new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }
}
