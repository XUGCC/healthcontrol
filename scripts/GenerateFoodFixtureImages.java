import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import javax.imageio.ImageIO;

public class GenerateFoodFixtureImages {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 900;
    private static final String URL_PREFIX = "http://localhost:7245/test-fixtures/foods/";

    private record FoodSpec(int id, String slug, String scene, String primaryHex, String secondaryHex) {
        String fileName() {
            return id + "-" + slug + ".png";
        }

        String url() {
            return URL_PREFIX + fileName();
        }
    }

    private record BowlShape(double x, double y, double w, double h) {}
    private record PlateShape(double x, double y, double w, double h) {}
    private record BoardShape(double x, double y, double w, double h) {}
    private record GlassShape(double x, double y, double w, double h) {}

    public static void main(String[] args) throws Exception {
        Path outputRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of("HealthControl.springboot", "external-resources", "test-fixtures", "foods").toAbsolutePath();
        Path sqlOutput = args.length > 1
                ? Path.of(args[1]).toAbsolutePath().normalize()
                : Path.of("food-realistic-image-updates.sql").toAbsolutePath();

        Files.createDirectories(outputRoot);
        if (sqlOutput.getParent() != null) {
            Files.createDirectories(sqlOutput.getParent());
        }

        List<FoodSpec> foods = foods();
        for (FoodSpec spec : foods) {
            renderImage(spec, outputRoot.resolve(spec.fileName()));
        }

        writeSql(sqlOutput, foods);
        System.out.println("Generated " + foods.size() + " food fixture images under " + outputRoot);
        System.out.println("Generated SQL file at " + sqlOutput);
    }

    private static List<FoodSpec> foods() {
        return List.of(
                new FoodSpec(30001, "warm-water", "warm-water", "#0F766E", "#99F6E4"),
                new FoodSpec(30002, "honey-water", "honey-water", "#A16207", "#FDE68A"),
                new FoodSpec(30003, "steamed-pear", "steamed-pear", "#3F6212", "#BEF264"),
                new FoodSpec(30004, "silver-fungus", "silver-fungus", "#A16207", "#FDE68A"),
                new FoodSpec(30005, "lemon-honey", "lemon-honey", "#CA8A04", "#FDE047"),
                new FoodSpec(30011, "millet-porridge", "millet-porridge", "#B45309", "#FED7AA"),
                new FoodSpec(30012, "steamed-egg", "steamed-egg", "#CA8A04", "#FEF08A"),
                new FoodSpec(30013, "tofu", "tofu", "#64748B", "#E2E8F0"),
                new FoodSpec(30014, "steamed-pumpkin", "steamed-pumpkin", "#EA580C", "#FDBA74"),
                new FoodSpec(30015, "rice-porridge", "rice-porridge", "#A8A29E", "#F5F5F4"),
                new FoodSpec(30021, "yam", "yam", "#7C3AED", "#DDD6FE"),
                new FoodSpec(30022, "lotus-soup", "lotus-soup", "#9A3412", "#FED7AA"),
                new FoodSpec(30023, "red-date", "red-date", "#991B1B", "#FCA5A5"),
                new FoodSpec(30024, "goji-berry", "goji-berry", "#B91C1C", "#FCA5A5"),
                new FoodSpec(30031, "blueberry", "blueberry", "#312E81", "#A5B4FC"),
                new FoodSpec(30032, "broccoli", "broccoli", "#166534", "#86EFAC"),
                new FoodSpec(30033, "kiwi", "kiwi", "#4D7C0F", "#BEF264"),
                new FoodSpec(30034, "steamed-apple", "steamed-apple", "#DC2626", "#FECACA"),
                new FoodSpec(30041, "steamed-fish", "steamed-fish", "#0F172A", "#7DD3FC"),
                new FoodSpec(30042, "chicken-breast", "chicken-breast", "#9A3412", "#FED7AA"),
                new FoodSpec(30043, "yogurt", "yogurt", "#7C3AED", "#E9D5FF"),
                new FoodSpec(30044, "soy-milk", "soy-milk", "#92400E", "#FDE68A"),
                new FoodSpec(40001, "chili", "chili", "#991B1B", "#FB7185"),
                new FoodSpec(40002, "hotpot", "hotpot", "#7F1D1D", "#FCA5A5"),
                new FoodSpec(40003, "wasabi", "wasabi", "#3F6212", "#A3E635"),
                new FoodSpec(40004, "peppercorn", "peppercorn", "#6B210A", "#D6A676"),
                new FoodSpec(40011, "fried-chicken", "fried-chicken", "#92400E", "#FCD34D"),
                new FoodSpec(40012, "bbq", "bbq", "#7C2D12", "#FB923C"),
                new FoodSpec(40013, "fries", "fries", "#B45309", "#FDE68A"),
                new FoodSpec(40014, "fried-dough", "fried-dough", "#A16207", "#FCD34D"),
                new FoodSpec(40021, "hot-soup", "hot-soup", "#B91C1C", "#FCA5A5"),
                new FoodSpec(40022, "hot-tea", "hot-tea", "#92400E", "#FDBA74"),
                new FoodSpec(40023, "hot-porridge", "hot-porridge", "#B45309", "#FDE68A"),
                new FoodSpec(40024, "hot-pot-hot", "hot-pot-hot", "#7F1D1D", "#FDBA74"),
                new FoodSpec(40031, "baijiu", "baijiu", "#1F2937", "#D1D5DB"),
                new FoodSpec(40032, "coffee", "coffee", "#78350F", "#D6A676"),
                new FoodSpec(40033, "beer", "beer", "#854D0E", "#FCD34D"),
                new FoodSpec(40034, "energy-drink", "energy-drink", "#1D4ED8", "#93C5FD"),
                new FoodSpec(40041, "milk-tea", "milk-tea", "#7C2D12", "#FDBA74"),
                new FoodSpec(40042, "chocolate", "chocolate", "#451A03", "#B45309"),
                new FoodSpec(40043, "cake", "cake", "#9D174D", "#F9A8D4"),
                new FoodSpec(40044, "ice-cream", "ice-cream", "#DB2777", "#FBCFE8")
        );
    }

    private static void renderImage(FoodSpec spec, Path target) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        enableQuality(g);

        Color primary = color(spec.primaryHex());
        Color secondary = color(spec.secondaryHex());
        Random random = new Random(spec.id() * 97L + spec.slug().hashCode());

        drawBackdrop(g, primary, secondary, random);
        drawScene(g, spec, random);
        addVignette(g);
        addFilmGrain(g, random);

        g.dispose();
        ImageIO.write(image, "png", target.toFile());
    }

    private static void writeSql(Path sqlOutput, List<FoodSpec> foods) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("-- Auto-generated by GenerateFoodFixtureImages.java");
        lines.add("START TRANSACTION;");
        for (FoodSpec spec : foods) {
            lines.add(String.format(Locale.ROOT,
                    "UPDATE `tlaryngealfood` SET `PicUrl`='%s' WHERE `Id`=%d;",
                    spec.url(), spec.id()));
        }
        lines.add("COMMIT;");
        Files.writeString(sqlOutput, String.join(System.lineSeparator(), lines) + System.lineSeparator(),
                StandardCharsets.UTF_8);
    }

    private static void enableQuality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    }

    private static Color color(String hex) {
        return Color.decode(hex);
    }

    private static Color alpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), clamp(alpha));
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static Color mix(Color a, Color b, double ratio) {
        double r = Math.max(0, Math.min(1, ratio));
        int red = (int) Math.round(a.getRed() * (1 - r) + b.getRed() * r);
        int green = (int) Math.round(a.getGreen() * (1 - r) + b.getGreen() * r);
        int blue = (int) Math.round(a.getBlue() * (1 - r) + b.getBlue() * r);
        int alpha = (int) Math.round(a.getAlpha() * (1 - r) + b.getAlpha() * r);
        return new Color(red, green, blue, alpha);
    }

    private static void drawBackdrop(Graphics2D g, Color primary, Color secondary, Random random) {
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(0, 0),
                new Point2D.Double(WIDTH, HEIGHT),
                new float[] {0f, 0.45f, 1f},
                new Color[] {
                        mix(primary, Color.WHITE, 0.82),
                        mix(secondary, Color.WHITE, 0.74),
                        mix(primary, Color.WHITE, 0.90)
                }));
        g.fill(new Rectangle2D.Double(0, 0, WIDTH, HEIGHT));

        paintTableSurface(g, primary, secondary, random);
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(WIDTH * 0.32, HEIGHT * 0.22),
                WIDTH * 0.38f,
                new float[] {0f, 1f},
                new Color[] {alpha(Color.WHITE, 56), new Color(255, 255, 255, 0)}));
        g.fill(new Ellipse2D.Double(0, 0, WIDTH * 0.7, HEIGHT * 0.6));
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(WIDTH * 0.74, HEIGHT * 0.84),
                WIDTH * 0.32f,
                new float[] {0f, 1f},
                new Color[] {alpha(mix(secondary, Color.WHITE, 0.25), 46), new Color(255, 255, 255, 0)}));
        g.fill(new Ellipse2D.Double(WIDTH * 0.48, HEIGHT * 0.55, WIDTH * 0.44, HEIGHT * 0.38));
    }

    private static void paintTableSurface(Graphics2D g, Color primary, Color secondary, Random random) {
        Paint original = g.getPaint();
        g.setPaint(new GradientPaint(0, 0, mix(primary, new Color(82, 58, 38), 0.55),
                0, HEIGHT, mix(secondary, new Color(214, 190, 170), 0.38)));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        for (int i = 0; i < 22; i++) {
            double y = HEIGHT * (i / 22.0);
            g.setColor(alpha(mix(primary, secondary, i / 21.0), 16 + random.nextInt(10)));
            g.fill(new Rectangle2D.Double(0, y, WIDTH, 6 + random.nextInt(10)));
        }

        for (int i = 0; i < 14; i++) {
            double cx = random.nextDouble() * WIDTH;
            double cy = random.nextDouble() * HEIGHT;
            double radius = 120 + random.nextDouble() * 180;
            g.setPaint(new RadialGradientPaint(
                    new Point2D.Double(cx, cy),
                    (float) radius,
                    new float[] {0f, 1f},
                    new Color[] {alpha(mix(primary, secondary, random.nextDouble()), 30), new Color(0, 0, 0, 0)}));
            g.fill(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));
        }

        g.setPaint(original);
    }

    private static void drawScene(Graphics2D g, FoodSpec spec, Random random) {
        switch (spec.scene()) {
            case "warm-water" -> renderWarmWater(g, spec, random);
            case "honey-water" -> renderHoneyWater(g, spec, random);
            case "steamed-pear" -> renderSteamedPear(g, spec, random);
            case "silver-fungus" -> renderSilverFungus(g, spec, random);
            case "lemon-honey" -> renderLemonHoney(g, spec, random);
            case "millet-porridge" -> renderMilletPorridge(g, spec, random);
            case "steamed-egg" -> renderSteamedEgg(g, spec, random);
            case "tofu" -> renderTofu(g, spec, random);
            case "steamed-pumpkin" -> renderSteamedPumpkin(g, spec, random);
            case "rice-porridge" -> renderRicePorridge(g, spec, random);
            case "yam" -> renderYam(g, spec, random);
            case "lotus-soup" -> renderLotusSoup(g, spec, random);
            case "red-date" -> renderRedDate(g, spec, random);
            case "goji-berry" -> renderGojiBerry(g, spec, random);
            case "blueberry" -> renderBlueberry(g, spec, random);
            case "broccoli" -> renderBroccoli(g, spec, random);
            case "kiwi" -> renderKiwi(g, spec, random);
            case "steamed-apple" -> renderSteamedApple(g, spec, random);
            case "steamed-fish" -> renderSteamedFish(g, spec, random);
            case "chicken-breast" -> renderChickenBreast(g, spec, random);
            case "yogurt" -> renderYogurt(g, spec, random);
            case "soy-milk" -> renderSoyMilk(g, spec, random);
            case "chili" -> renderChili(g, spec, random);
            case "hotpot" -> renderHotpot(g, spec, random);
            case "wasabi" -> renderWasabi(g, spec, random);
            case "peppercorn" -> renderPeppercorn(g, spec, random);
            case "fried-chicken" -> renderFriedChicken(g, spec, random);
            case "bbq" -> renderBbq(g, spec, random);
            case "fries" -> renderFries(g, spec, random);
            case "fried-dough" -> renderFriedDough(g, spec, random);
            case "hot-soup" -> renderHotSoup(g, spec, random);
            case "hot-tea" -> renderHotTea(g, spec, random);
            case "hot-porridge" -> renderHotPorridge(g, spec, random);
            case "hot-pot-hot" -> renderHotPotHot(g, spec, random);
            case "baijiu" -> renderBaijiu(g, spec, random);
            case "coffee" -> renderCoffee(g, spec, random);
            case "beer" -> renderBeer(g, spec, random);
            case "energy-drink" -> renderEnergyDrink(g, spec, random);
            case "milk-tea" -> renderMilkTea(g, spec, random);
            case "chocolate" -> renderChocolate(g, spec, random);
            case "cake" -> renderCake(g, spec, random);
            case "ice-cream" -> renderIceCream(g, spec, random);
            default -> renderFallback(g, spec, random);
        }
    }

    private static void addVignette(Graphics2D g) {
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(WIDTH / 2.0, HEIGHT / 2.0),
                WIDTH * 0.72f,
                new float[] {0.55f, 1f},
                new Color[] {new Color(0, 0, 0, 0), new Color(12, 16, 18, 34)}));
        g.fill(new Rectangle2D.Double(0, 0, WIDTH, HEIGHT));
    }

    private static void addFilmGrain(Graphics2D g, Random random) {
        for (int i = 0; i < 4200; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            int alpha = 5 + random.nextInt(10);
            int size = random.nextInt(3) + 1;
            g.setColor(new Color(255, 255, 255, alpha));
            g.fillRect(x, y, size, size);
        }
        for (int i = 0; i < 2200; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            int alpha = 3 + random.nextInt(8);
            int size = random.nextInt(2) + 1;
            g.setColor(new Color(0, 0, 0, alpha));
            g.fillRect(x, y, size, size);
        }
    }

    private static void drawShadow(Graphics2D g, Shape shape, double dx, double dy, int alpha) {
        AffineTransform transform = AffineTransform.getTranslateInstance(dx, dy);
        Shape shifted = transform.createTransformedShape(shape);
        g.setColor(new Color(12, 16, 18, clamp(alpha)));
        g.fill(shifted);
    }

    private static BowlShape drawBowl(Graphics2D g, double x, double y, double w, double h, Color rim, Color body) {
        Ellipse2D outer = new Ellipse2D.Double(x, y, w, h * 0.54);
        Ellipse2D inner = new Ellipse2D.Double(x + w * 0.08, y + h * 0.03, w * 0.84, h * 0.42);
        Arc2D bodyArc = new Arc2D.Double(x, y + h * 0.08, w, h * 0.95, 0, 180, Arc2D.PIE);
        drawShadow(g, new Ellipse2D.Double(x + w * 0.06, y + h * 0.64, w * 0.88, h * 0.20), 0, 18, 24);

        g.setPaint(new GradientPaint((float) x, (float) y, rim, (float) (x + w), (float) (y + h * 0.30), mix(rim, Color.WHITE, 0.35)));
        g.fill(outer);
        g.setColor(mix(rim, Color.WHITE, 0.92));
        g.fill(inner);
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y + h * 0.15),
                new Point2D.Double(x, y + h),
                new float[] {0f, 1f},
                new Color[] {mix(body, Color.WHITE, 0.18), body}));
        g.fill(bodyArc);
        g.setColor(alpha(Color.WHITE, 80));
        g.setStroke(new BasicStroke(4f));
        g.draw(new Arc2D.Double(x + w * 0.08, y + h * 0.07, w * 0.84, h * 0.34, 180, 160, Arc2D.OPEN));
        return new BowlShape(x + w * 0.13, y + h * 0.08, w * 0.74, h * 0.24);
    }

    private static PlateShape drawPlate(Graphics2D g, double x, double y, double w, double h, Color rim, Color center) {
        Ellipse2D outer = new Ellipse2D.Double(x, y, w, h);
        Ellipse2D inner = new Ellipse2D.Double(x + w * 0.08, y + h * 0.09, w * 0.84, h * 0.72);
        drawShadow(g, new Ellipse2D.Double(x + w * 0.08, y + h * 0.78, w * 0.84, h * 0.16), 0, 16, 24);
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x + w, y + h),
                new float[] {0f, 1f},
                new Color[] {mix(rim, Color.WHITE, 0.32), rim}));
        g.fill(outer);
        g.setColor(center);
        g.fill(inner);
        g.setColor(alpha(Color.WHITE, 92));
        g.setStroke(new BasicStroke(4f));
        g.draw(new Arc2D.Double(x + w * 0.1, y + h * 0.12, w * 0.8, h * 0.24, 180, 155, Arc2D.OPEN));
        return new PlateShape(x + w * 0.18, y + h * 0.18, w * 0.64, h * 0.44);
    }

    private static BoardShape drawBoard(Graphics2D g, double x, double y, double w, double h, Color wood) {
        RoundRectangle2D board = new RoundRectangle2D.Double(x, y, w, h, 38, 38);
        drawShadow(g, new RoundRectangle2D.Double(x + 12, y + h * 0.86, w * 0.88, h * 0.18, 26, 26), 0, 8, 22);
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x + w, y + h),
                new float[] {0f, 0.5f, 1f},
                new Color[] {mix(wood, Color.WHITE, 0.30), wood, mix(wood, new Color(90, 55, 24), 0.25)}));
        g.fill(board);
        g.setColor(alpha(mix(wood, Color.WHITE, 0.42), 80));
        for (int i = 0; i < 10; i++) {
            double yy = y + 26 + i * (h - 52) / 10.0;
            g.fill(new RoundRectangle2D.Double(x + 18, yy, w - 36, 6, 6, 6));
        }
        return new BoardShape(x + 34, y + 28, w - 68, h - 56);
    }

    private static GlassShape drawGlass(Graphics2D g, double x, double y, double w, double h, Color liquid, boolean withIce) {
        Path2D glass = new Path2D.Double();
        glass.moveTo(x + w * 0.18, y);
        glass.lineTo(x + w * 0.82, y);
        glass.lineTo(x + w * 0.68, y + h);
        glass.lineTo(x + w * 0.32, y + h);
        glass.closePath();
        drawShadow(g, new Ellipse2D.Double(x + w * 0.10, y + h * 0.92, w * 0.80, h * 0.14), 0, 10, 22);
        g.setColor(new Color(255, 255, 255, 64));
        g.fill(glass);
        g.setColor(new Color(255, 255, 255, 120));
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(glass);

        Path2D fill = new Path2D.Double();
        fill.moveTo(x + w * 0.26, y + h * 0.36);
        fill.lineTo(x + w * 0.74, y + h * 0.36);
        fill.lineTo(x + w * 0.63, y + h * 0.88);
        fill.lineTo(x + w * 0.37, y + h * 0.88);
        fill.closePath();
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x, y + h),
                new float[] {0f, 1f},
                new Color[] {mix(liquid, Color.WHITE, 0.26), liquid}));
        g.fill(fill);

        if (withIce) {
            for (int i = 0; i < 4; i++) {
                double ix = x + w * (0.30 + i * 0.11);
                double iy = y + h * (0.16 + (i % 2) * 0.07);
                g.setColor(new Color(255, 255, 255, 78));
                g.fill(new RoundRectangle2D.Double(ix, iy, w * 0.12, h * 0.10, 10, 10));
            }
        }

        g.setColor(new Color(255, 255, 255, 44));
        g.fill(new RoundRectangle2D.Double(x + w * 0.28, y + h * 0.10, w * 0.10, h * 0.72, 18, 18));
        return new GlassShape(x, y, w, h);
    }

    private static BowlShape drawCup(Graphics2D g, double x, double y, double w, double h, Color cup, Color drink) {
        RoundRectangle2D body = new RoundRectangle2D.Double(x, y, w, h, 40, 40);
        drawShadow(g, new Ellipse2D.Double(x + w * 0.06, y + h * 0.90, w * 0.88, h * 0.16), 0, 10, 22);
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x + w, y + h),
                new float[] {0f, 1f},
                new Color[] {mix(cup, Color.WHITE, 0.28), cup}));
        g.fill(body);
        g.setColor(new Color(255, 255, 255, 70));
        g.fill(new RoundRectangle2D.Double(x + w * 0.12, y + h * 0.08, w * 0.24, h * 0.18, 18, 18));
        g.setColor(mix(cup, Color.WHITE, 0.86));
        g.fill(new RoundRectangle2D.Double(x + w * 0.08, y + h * 0.08, w * 0.84, h * 0.60, 30, 30));
        Ellipse2D drinkTop = new Ellipse2D.Double(x + w * 0.12, y + h * 0.14, w * 0.76, h * 0.24);
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(x + w * 0.44, y + h * 0.20),
                (float) (w * 0.42),
                new float[] {0f, 1f},
                new Color[] {mix(drink, Color.WHITE, 0.22), drink}));
        g.fill(drinkTop);
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(alpha(mix(cup, new Color(120, 120, 120), 0.20), 120));
        g.draw(new Arc2D.Double(x + w * 0.74, y + h * 0.22, w * 0.38, h * 0.38, 270, 250, Arc2D.OPEN));
        return new BowlShape(x + w * 0.12, y + h * 0.14, w * 0.76, h * 0.24);
    }

    private static void drawNapkin(Graphics2D g, double x, double y, double w, double h, Color color, double angle) {
        AffineTransform previous = g.getTransform();
        g.rotate(Math.toRadians(angle), x + w / 2.0, y + h / 2.0);
        RoundRectangle2D cloth = new RoundRectangle2D.Double(x, y, w, h, 30, 30);
        drawShadow(g, new RoundRectangle2D.Double(x + 10, y + h * 0.76, w * 0.84, h * 0.20, 22, 22), 0, 4, 18);
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x + w, y + h),
                new float[] {0f, 1f},
                new Color[] {mix(color, Color.WHITE, 0.18), mix(color, new Color(180, 180, 180), 0.18)}));
        g.fill(cloth);
        g.setColor(alpha(Color.WHITE, 60));
        for (int i = 1; i <= 3; i++) {
            g.fill(new RoundRectangle2D.Double(x + 20, y + i * h / 4.0, w - 40, 5, 5, 5));
        }
        g.setTransform(previous);
    }

    private static void drawCutlery(Graphics2D g, double x, double y, double length, boolean horizontal) {
        AffineTransform previous = g.getTransform();
        if (horizontal) {
            g.rotate(Math.toRadians(90), x + length / 2.0, y + length / 2.0);
        }
        g.setColor(new Color(255, 255, 255, 26));
        g.fill(new RoundRectangle2D.Double(x - 10, y, 24, length * 0.76, 16, 16));
        g.fill(new RoundRectangle2D.Double(x + 28, y + 20, 20, length * 0.70, 16, 16));
        g.fill(new Ellipse2D.Double(x - 26, y - 6, 56, 74));
        g.fill(new RoundRectangle2D.Double(x + 22, y - 2, 36, 78, 22, 22));
        g.setTransform(previous);
    }

    private static void drawSteam(Graphics2D g, double x, double y, double scale, int count) {
        g.setStroke(new BasicStroke((float) (4 * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < count; i++) {
            double offset = (i - (count - 1) / 2.0) * 20 * scale;
            Path2D path = new Path2D.Double();
            path.moveTo(x + offset, y);
            path.curveTo(x - 10 * scale + offset, y - 32 * scale, x + 16 * scale + offset, y - 68 * scale,
                    x + offset, y - 110 * scale);
            g.setColor(new Color(255, 255, 255, 88));
            g.draw(path);
        }
    }

    private static void scatterSesame(Graphics2D g, double x, double y, double w, double h, int count, Random random) {
        for (int i = 0; i < count; i++) {
            double px = x + random.nextDouble() * w;
            double py = y + random.nextDouble() * h;
            double rx = 5 + random.nextDouble() * 6;
            double ry = 2 + random.nextDouble() * 3;
            double angle = random.nextDouble() * Math.PI;
            AffineTransform previous = g.getTransform();
            g.rotate(angle, px, py);
            g.setColor(new Color(247, 236, 186, 180));
            g.fill(new Ellipse2D.Double(px - rx, py - ry, rx * 2, ry * 2));
            g.setTransform(previous);
        }
    }

    private static void drawHerbLeaves(Graphics2D g, double x, double y, double scale, int count, Color base, Random random) {
        for (int i = 0; i < count; i++) {
            double px = x + random.nextDouble() * 80 * scale;
            double py = y + random.nextDouble() * 50 * scale;
            double w = (18 + random.nextDouble() * 16) * scale;
            double h = (8 + random.nextDouble() * 10) * scale;
            double angle = -35 + random.nextDouble() * 70;
            AffineTransform previous = g.getTransform();
            g.rotate(Math.toRadians(angle), px, py);
            g.setPaint(new GradientPaint((float) (px - w / 2), (float) py, mix(base, Color.WHITE, 0.22),
                    (float) (px + w / 2), (float) py, mix(base, new Color(20, 60, 20), 0.24)));
            g.fill(new Ellipse2D.Double(px - w / 2, py - h / 2, w, h));
            g.setColor(alpha(Color.WHITE, 70));
            g.fill(new RoundRectangle2D.Double(px - w * 0.03, py - h * 0.42, w * 0.06, h * 0.84, 4, 4));
            g.setTransform(previous);
        }
    }

    private static void drawPear(Graphics2D g, double x, double y, double scale, boolean halved) {
        Path2D pear = new Path2D.Double();
        pear.moveTo(x + 30 * scale, y + 4 * scale);
        pear.curveTo(x - 6 * scale, y + 18 * scale, x - 10 * scale, y + 96 * scale, x + 22 * scale, y + 138 * scale);
        pear.curveTo(x + 44 * scale, y + 165 * scale, x + 98 * scale, y + 162 * scale, x + 118 * scale, y + 130 * scale);
        pear.curveTo(x + 144 * scale, y + 90 * scale, x + 136 * scale, y + 20 * scale, x + 82 * scale, y + 2 * scale);
        pear.closePath();
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x + 120 * scale, y + 150 * scale),
                new float[] {0f, 0.65f, 1f},
                new Color[] {new Color(238, 247, 184), new Color(194, 216, 94), new Color(132, 163, 60)}));
        g.fill(pear);
        g.setColor(new Color(148, 92, 42));
        g.fill(new RoundRectangle2D.Double(x + 58 * scale, y - 12 * scale, 8 * scale, 28 * scale, 5, 5));
        g.setColor(alpha(Color.WHITE, 70));
        g.fill(new Ellipse2D.Double(x + 16 * scale, y + 18 * scale, 36 * scale, 42 * scale));
        if (halved) {
            g.setColor(new Color(247, 239, 210));
            g.fill(new Ellipse2D.Double(x + 18 * scale, y + 24 * scale, 82 * scale, 98 * scale));
            g.setColor(new Color(155, 117, 62));
            g.fill(new Ellipse2D.Double(x + 46 * scale, y + 56 * scale, 10 * scale, 18 * scale));
            g.fill(new Ellipse2D.Double(x + 64 * scale, y + 56 * scale, 10 * scale, 18 * scale));
        }
    }

    private static void drawLemonSlice(Graphics2D g, double x, double y, double radius) {
        g.setColor(new Color(250, 212, 70));
        g.fill(new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2));
        g.setColor(new Color(255, 243, 179));
        g.fill(new Ellipse2D.Double(x - radius * 0.84, y - radius * 0.84, radius * 1.68, radius * 1.68));
        g.setColor(new Color(249, 227, 112));
        g.fill(new Ellipse2D.Double(x - radius * 0.18, y - radius * 0.18, radius * 0.36, radius * 0.36));
        g.setStroke(new BasicStroke((float) (radius * 0.08)));
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60);
            double x2 = x + Math.cos(angle) * radius * 0.72;
            double y2 = y + Math.sin(angle) * radius * 0.72;
            g.setColor(new Color(248, 235, 160));
            g.draw(new java.awt.geom.Line2D.Double(x, y, x2, y2));
        }
    }

    private static void drawBlueberries(Graphics2D g, double x, double y, int count, double scale, Random random) {
        for (int i = 0; i < count; i++) {
            double px = x + (i % 3) * 50 * scale + random.nextDouble() * 12 * scale;
            double py = y + (i / 3) * 42 * scale + random.nextDouble() * 10 * scale;
            double size = (30 + random.nextDouble() * 10) * scale;
            g.setPaint(new RadialGradientPaint(
                    new Point2D.Double(px + size * 0.38, py + size * 0.34),
                    (float) (size * 0.8),
                    new float[] {0f, 1f},
                    new Color[] {new Color(92, 98, 212), new Color(50, 48, 126)}));
            g.fill(new Ellipse2D.Double(px, py, size, size));
            g.setColor(new Color(190, 197, 248, 110));
            g.fill(new Ellipse2D.Double(px + size * 0.16, py + size * 0.18, size * 0.26, size * 0.24));
        }
    }

    private static void drawBroccoliFloret(Graphics2D g, double x, double y, double scale) {
        g.setColor(new Color(106, 159, 84));
        g.fill(new RoundRectangle2D.Double(x + 46 * scale, y + 72 * scale, 24 * scale, 60 * scale, 10, 10));
        Color leaf = new Color(52, 126, 70);
        g.setPaint(new GradientPaint((float) x, (float) y, mix(leaf, Color.WHITE, 0.12), (float) (x + 90 * scale), (float) (y + 90 * scale), leaf));
        g.fill(new Ellipse2D.Double(x + 6 * scale, y + 36 * scale, 42 * scale, 44 * scale));
        g.fill(new Ellipse2D.Double(x + 28 * scale, y + 20 * scale, 48 * scale, 48 * scale));
        g.fill(new Ellipse2D.Double(x + 56 * scale, y + 30 * scale, 42 * scale, 44 * scale));
        g.setColor(alpha(Color.WHITE, 36));
        g.fill(new Ellipse2D.Double(x + 32 * scale, y + 30 * scale, 18 * scale, 16 * scale));
    }

    private static void drawKiwiSlice(Graphics2D g, double x, double y, double radius) {
        g.setColor(new Color(119, 71, 28));
        g.fill(new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2));
        g.setColor(new Color(151, 214, 72));
        g.fill(new Ellipse2D.Double(x - radius * 0.84, y - radius * 0.84, radius * 1.68, radius * 1.68));
        g.setColor(new Color(246, 236, 208));
        g.fill(new Ellipse2D.Double(x - radius * 0.28, y - radius * 0.28, radius * 0.56, radius * 0.56));
        for (int i = 0; i < 20; i++) {
            double angle = Math.toRadians(i * 18);
            double px = x + Math.cos(angle) * radius * 0.46;
            double py = y + Math.sin(angle) * radius * 0.46;
            g.setColor(new Color(36, 29, 24));
            g.fill(new Ellipse2D.Double(px - radius * 0.04, py - radius * 0.02, radius * 0.08, radius * 0.04));
        }
    }

    private static void drawAppleSlices(Graphics2D g, double x, double y, double scale) {
        for (int i = 0; i < 4; i++) {
            double px = x + i * 36 * scale;
            Path2D slice = new Path2D.Double();
            slice.moveTo(px, y + 48 * scale);
            slice.curveTo(px + 10 * scale, y, px + 78 * scale, y, px + 88 * scale, y + 48 * scale);
            slice.curveTo(px + 82 * scale, y + 88 * scale, px + 8 * scale, y + 88 * scale, px, y + 48 * scale);
            g.setPaint(new LinearGradientPaint(
                    new Point2D.Double(px, y),
                    new Point2D.Double(px + 88 * scale, y + 88 * scale),
                    new float[] {0f, 0.7f, 1f},
                    new Color[] {new Color(255, 240, 208), new Color(250, 215, 160), new Color(218, 62, 62)}));
            g.fill(slice);
            g.setColor(new Color(171, 103, 46));
            g.fill(new Ellipse2D.Double(px + 40 * scale, y + 34 * scale, 7 * scale, 16 * scale));
        }
    }

    private static void drawTofuCube(Graphics2D g, double x, double y, double size) {
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x + size, y + size),
                new float[] {0f, 1f},
                new Color[] {new Color(249, 246, 236), new Color(226, 217, 196)}));
        g.fill(new RoundRectangle2D.Double(x, y, size, size, size * 0.16, size * 0.16));
        g.setColor(new Color(255, 255, 255, 90));
        g.fill(new RoundRectangle2D.Double(x + size * 0.08, y + size * 0.08, size * 0.34, size * 0.18, 8, 8));
    }

    private static void drawRedDates(Graphics2D g, double x, double y, int count, double scale, Random random) {
        for (int i = 0; i < count; i++) {
            double px = x + (i % 3) * 56 * scale + random.nextDouble() * 10 * scale;
            double py = y + (i / 3) * 40 * scale + random.nextDouble() * 8 * scale;
            double w = (44 + random.nextDouble() * 8) * scale;
            double h = (28 + random.nextDouble() * 8) * scale;
            g.setPaint(new LinearGradientPaint(
                    new Point2D.Double(px, py),
                    new Point2D.Double(px + w, py + h),
                    new float[] {0f, 0.5f, 1f},
                    new Color[] {new Color(122, 29, 25), new Color(164, 48, 35), new Color(96, 18, 16)}));
            g.fill(new Ellipse2D.Double(px, py, w, h));
            g.setColor(new Color(255, 255, 255, 46));
            g.fill(new Ellipse2D.Double(px + w * 0.18, py + h * 0.18, w * 0.22, h * 0.20));
        }
    }

    private static void drawGojiPile(Graphics2D g, double x, double y, int count, double scale, Random random) {
        for (int i = 0; i < count; i++) {
            double px = x + random.nextDouble() * 120 * scale;
            double py = y + random.nextDouble() * 80 * scale;
            double w = (14 + random.nextDouble() * 6) * scale;
            double h = (26 + random.nextDouble() * 8) * scale;
            double angle = -30 + random.nextDouble() * 60;
            AffineTransform previous = g.getTransform();
            g.rotate(Math.toRadians(angle), px, py);
            g.setPaint(new GradientPaint((float) px, (float) py, new Color(225, 74, 46), (float) (px + w), (float) (py + h), new Color(162, 22, 22)));
            g.fill(new Ellipse2D.Double(px - w / 2, py - h / 2, w, h));
            g.setTransform(previous);
        }
    }

    private static void drawChilies(Graphics2D g, double x, double y, int count, double scale, Random random) {
        for (int i = 0; i < count; i++) {
            double px = x + (i % 3) * 80 * scale + random.nextDouble() * 16 * scale;
            double py = y + (i / 3) * 56 * scale + random.nextDouble() * 10 * scale;
            double angle = -44 + random.nextDouble() * 88;
            AffineTransform previous = g.getTransform();
            g.rotate(Math.toRadians(angle), px, py);
            Path2D chili = new Path2D.Double();
            chili.moveTo(px - 46 * scale, py);
            chili.curveTo(px - 12 * scale, py - 30 * scale, px + 46 * scale, py - 20 * scale, px + 54 * scale, py + 4 * scale);
            chili.curveTo(px + 36 * scale, py + 18 * scale, px + 6 * scale, py + 24 * scale, px - 48 * scale, py + 12 * scale);
            chili.closePath();
            g.setPaint(new LinearGradientPaint(
                    new Point2D.Double(px - 40 * scale, py),
                    new Point2D.Double(px + 54 * scale, py + 14 * scale),
                    new float[] {0f, 0.65f, 1f},
                    new Color[] {new Color(255, 104, 90), new Color(203, 34, 34), new Color(120, 8, 12)}));
            g.fill(chili);
            g.setColor(new Color(61, 123, 58));
            g.fill(new Ellipse2D.Double(px + 44 * scale, py - 6 * scale, 14 * scale, 12 * scale));
            g.setTransform(previous);
        }
    }

    private static void drawPeppercorns(Graphics2D g, double x, double y, int count, double scale, Random random) {
        for (int i = 0; i < count; i++) {
            double px = x + random.nextDouble() * 140 * scale;
            double py = y + random.nextDouble() * 90 * scale;
            double size = (12 + random.nextDouble() * 8) * scale;
            Color c = switch (i % 3) {
                case 0 -> new Color(99, 44, 30);
                case 1 -> new Color(56, 30, 24);
                default -> new Color(110, 92, 60);
            };
            g.setPaint(new RadialGradientPaint(
                    new Point2D.Double(px + size * 0.35, py + size * 0.35),
                    (float) size,
                    new float[] {0f, 1f},
                    new Color[] {mix(c, Color.WHITE, 0.20), c}));
            g.fill(new Ellipse2D.Double(px, py, size, size));
        }
    }

    private static void drawFriesPile(Graphics2D g, double x, double y, int count, double scale, Random random) {
        for (int i = 0; i < count; i++) {
            double px = x + random.nextDouble() * 150 * scale;
            double py = y + random.nextDouble() * 70 * scale;
            double w = (14 + random.nextDouble() * 4) * scale;
            double h = (80 + random.nextDouble() * 22) * scale;
            double angle = -18 + random.nextDouble() * 36;
            AffineTransform previous = g.getTransform();
            g.rotate(Math.toRadians(angle), px, py);
            g.setPaint(new LinearGradientPaint(
                    new Point2D.Double(px, py),
                    new Point2D.Double(px + w, py + h),
                    new float[] {0f, 0.65f, 1f},
                    new Color[] {new Color(255, 224, 120), new Color(247, 194, 73), new Color(194, 123, 34)}));
            g.fill(new RoundRectangle2D.Double(px, py, w, h, 8, 8));
            g.setTransform(previous);
        }
    }

    private static void drawChocolatePieces(Graphics2D g, double x, double y, double scale, Random random) {
        for (int i = 0; i < 6; i++) {
            double px = x + (i % 3) * 80 * scale;
            double py = y + (i / 3) * 78 * scale;
            double angle = -14 + random.nextDouble() * 28;
            AffineTransform previous = g.getTransform();
            g.rotate(Math.toRadians(angle), px + 36 * scale, py + 30 * scale);
            g.setPaint(new LinearGradientPaint(
                    new Point2D.Double(px, py),
                    new Point2D.Double(px + 72 * scale, py + 60 * scale),
                    new float[] {0f, 1f},
                    new Color[] {new Color(111, 62, 33), new Color(66, 28, 15)}));
            g.fill(new RoundRectangle2D.Double(px, py, 72 * scale, 60 * scale, 14, 14));
            g.setColor(new Color(130, 88, 52));
            g.draw(new java.awt.geom.Line2D.Double(px + 24 * scale, py, px + 24 * scale, py + 60 * scale));
            g.draw(new java.awt.geom.Line2D.Double(px + 48 * scale, py, px + 48 * scale, py + 60 * scale));
            g.draw(new java.awt.geom.Line2D.Double(px, py + 20 * scale, px + 72 * scale, py + 20 * scale));
            g.draw(new java.awt.geom.Line2D.Double(px, py + 40 * scale, px + 72 * scale, py + 40 * scale));
            g.setTransform(previous);
        }
    }

    private static void drawBubbles(Graphics2D g, double x, double y, double w, double h, int count, Random random) {
        for (int i = 0; i < count; i++) {
            double size = 8 + random.nextDouble() * 20;
            double px = x + random.nextDouble() * w;
            double py = y + random.nextDouble() * h;
            g.setColor(new Color(255, 255, 255, 26 + random.nextInt(40)));
            g.fill(new Ellipse2D.Double(px, py, size, size));
        }
    }

    private static void drawLiquidSurface(Graphics2D g, BowlShape bowl, Color main, Color edge) {
        Ellipse2D surface = new Ellipse2D.Double(bowl.x(), bowl.y(), bowl.w(), bowl.h());
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(bowl.x() + bowl.w() * 0.45, bowl.y() + bowl.h() * 0.35),
                (float) (bowl.w() * 0.7),
                new float[] {0f, 0.78f, 1f},
                new Color[] {mix(main, Color.WHITE, 0.25), main, edge}));
        g.fill(surface);
        g.setColor(new Color(255, 255, 255, 60));
        g.fill(new Ellipse2D.Double(bowl.x() + bowl.w() * 0.14, bowl.y() + bowl.h() * 0.10, bowl.w() * 0.26, bowl.h() * 0.26));
    }

    private static void drawWasabiDollop(Graphics2D g, double x, double y, double scale) {
        Path2D blob = new Path2D.Double();
        blob.moveTo(x, y + 50 * scale);
        blob.curveTo(x - 18 * scale, y + 18 * scale, x + 10 * scale, y - 6 * scale, x + 42 * scale, y + 10 * scale);
        blob.curveTo(x + 72 * scale, y + 24 * scale, x + 72 * scale, y + 66 * scale, x + 48 * scale, y + 82 * scale);
        blob.curveTo(x + 18 * scale, y + 98 * scale, x - 4 * scale, y + 86 * scale, x, y + 50 * scale);
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x + 64 * scale, y + 84 * scale),
                new float[] {0f, 0.7f, 1f},
                new Color[] {new Color(190, 232, 105), new Color(112, 174, 46), new Color(74, 116, 28)}));
        g.fill(blob);
        g.setColor(new Color(255, 255, 255, 58));
        g.fill(new Ellipse2D.Double(x + 10 * scale, y + 20 * scale, 18 * scale, 14 * scale));
    }

    private static void drawFishBody(Graphics2D g, double x, double y, double scale) {
        Path2D fish = new Path2D.Double();
        fish.moveTo(x, y + 40 * scale);
        fish.curveTo(x + 60 * scale, y - 10 * scale, x + 200 * scale, y, x + 256 * scale, y + 48 * scale);
        fish.curveTo(x + 200 * scale, y + 90 * scale, x + 56 * scale, y + 96 * scale, x, y + 40 * scale);
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x + 256 * scale, y + 96 * scale),
                new float[] {0f, 0.65f, 1f},
                new Color[] {new Color(214, 232, 241), new Color(155, 197, 210), new Color(114, 160, 178)}));
        g.fill(fish);
        Path2D tail = new Path2D.Double();
        tail.moveTo(x + 242 * scale, y + 48 * scale);
        tail.lineTo(x + 316 * scale, y + 10 * scale);
        tail.lineTo(x + 310 * scale, y + 92 * scale);
        tail.closePath();
        g.setColor(new Color(133, 176, 192));
        g.fill(tail);
        g.setColor(new Color(38, 56, 72));
        g.fill(new Ellipse2D.Double(x + 48 * scale, y + 42 * scale, 8 * scale, 8 * scale));
    }

    private static void drawChickenSlices(Graphics2D g, double x, double y, int count, double scale, Random random) {
        for (int i = 0; i < count; i++) {
            double px = x + i * 46 * scale;
            double py = y + (i % 2) * 12 * scale;
            g.setPaint(new LinearGradientPaint(
                    new Point2D.Double(px, py),
                    new Point2D.Double(px + 72 * scale, py + 48 * scale),
                    new float[] {0f, 0.75f, 1f},
                    new Color[] {new Color(253, 226, 196), new Color(232, 193, 154), new Color(176, 132, 88)}));
            g.fill(new Ellipse2D.Double(px, py, 82 * scale, 44 * scale));
            g.setColor(new Color(255, 255, 255, 48));
            g.fill(new Ellipse2D.Double(px + 10 * scale, py + 8 * scale, 24 * scale, 10 * scale));
        }
    }

    private static void drawYamSlices(Graphics2D g, double x, double y, int count, double scale) {
        for (int i = 0; i < count; i++) {
            double px = x + i * 48 * scale;
            g.setColor(new Color(123, 92, 78));
            g.fill(new Ellipse2D.Double(px, y, 82 * scale, 82 * scale));
            g.setColor(new Color(244, 237, 229));
            g.fill(new Ellipse2D.Double(px + 7 * scale, y + 7 * scale, 68 * scale, 68 * scale));
            g.setColor(new Color(225, 215, 200));
            g.draw(new Ellipse2D.Double(px + 18 * scale, y + 18 * scale, 46 * scale, 46 * scale));
        }
    }

    private static void drawLotusSlice(Graphics2D g, double x, double y, double scale) {
        g.setColor(new Color(229, 208, 184));
        g.fill(new Ellipse2D.Double(x, y, 84 * scale, 84 * scale));
        g.setColor(new Color(199, 171, 144));
        for (double[] p : new double[][] {{0.34, 0.28}, {0.5, 0.24}, {0.68, 0.32}, {0.34, 0.54}, {0.5, 0.60}, {0.66, 0.54}}) {
            g.fill(new Ellipse2D.Double(x + 84 * scale * p[0] - 7 * scale, y + 84 * scale * p[1] - 7 * scale, 14 * scale, 14 * scale));
        }
    }

    private static void renderWarmWater(Graphics2D g, FoodSpec spec, Random random) {
        GlassShape glass = drawGlass(g, 430, 160, 270, 500, new Color(166, 210, 232, 130), false);
        drawSteam(g, 565, 170, 1.15, 3);
        g.setColor(new Color(255, 255, 255, 34));
        g.fill(new Ellipse2D.Double(glass.x() + 38, glass.y() + 36, 56, 260));
    }

    private static void renderHoneyWater(Graphics2D g, FoodSpec spec, Random random) {
        BowlShape cup = drawCup(g, 390, 220, 320, 260, new Color(247, 236, 214), new Color(214, 163, 58));
        drawSteam(g, 545, 220, 1.0, 3);
        g.setColor(new Color(226, 190, 84));
        g.fill(new Ellipse2D.Double(680, 470, 90, 90));
        g.setColor(new Color(161, 112, 34));
        g.fill(new RoundRectangle2D.Double(715, 420, 18, 86, 10, 10));
        g.fill(new RoundRectangle2D.Double(700, 450, 48, 18, 10, 10));
        scatterSesame(g, cup.x(), cup.y(), cup.w(), cup.h(), 18, random);
    }

    private static void renderSteamedPear(Graphics2D g, FoodSpec spec, Random random) {
        BowlShape bowl = drawBowl(g, 270, 300, 660, 280, new Color(238, 234, 226), new Color(173, 150, 130));
        drawLiquidSurface(g, bowl, new Color(244, 232, 180), new Color(217, 189, 118));
        drawPear(g, 390, 250, 1.25, true);
        drawPear(g, 580, 266, 1.05, false);
        drawSteam(g, 565, 250, 0.95, 3);
    }

    private static void renderSilverFungus(Graphics2D g, FoodSpec spec, Random random) {
        BowlShape bowl = drawBowl(g, 270, 300, 660, 280, new Color(236, 232, 222), new Color(165, 138, 108));
        drawLiquidSurface(g, bowl, new Color(240, 214, 145), new Color(205, 171, 98));
        for (int i = 0; i < 6; i++) {
            double px = bowl.x() + 30 + i * 75;
            double py = bowl.y() + 18 + (i % 2) * 16;
            g.setPaint(new RadialGradientPaint(new Point2D.Double(px + 26, py + 22), 34f,
                    new float[] {0f, 1f},
                    new Color[] {new Color(255, 250, 240, 210), new Color(235, 224, 196, 190)}));
            g.fill(new Ellipse2D.Double(px, py, 70, 52));
        }
        drawRedDates(g, bowl.x() + 90, bowl.y() + 34, 4, 0.72, random);
    }

    private static void renderLemonHoney(Graphics2D g, FoodSpec spec, Random random) {
        GlassShape glass = drawGlass(g, 410, 170, 300, 510, new Color(235, 198, 82), true);
        drawSteam(g, 560, 175, 1.05, 3);
        drawLemonSlice(g, 485, 270, 50);
        drawLemonSlice(g, 622, 332, 44);
        g.setColor(new Color(205, 151, 42, 130));
        g.fill(new Ellipse2D.Double(500, 340, 130, 180));
    }

    private static void renderMilletPorridge(Graphics2D g, FoodSpec spec, Random random) {
        BowlShape bowl = drawBowl(g, 260, 300, 680, 300, new Color(242, 238, 230), new Color(176, 140, 102));
        drawLiquidSurface(g, bowl, new Color(235, 194, 106), new Color(196, 145, 64));
        scatterSesame(g, bowl.x() + 20, bowl.y() + 8, bowl.w() - 40, bowl.h() - 16, 64, random);
        drawSteam(g, 575, 250, 1.0, 3);
    }

    private static void renderSteamedEgg(Graphics2D g, FoodSpec spec, Random random) {
        BowlShape bowl = drawBowl(g, 290, 320, 620, 260, new Color(238, 232, 226), new Color(176, 144, 118));
        drawLiquidSurface(g, bowl, new Color(245, 214, 108), new Color(214, 170, 58));
        g.setColor(new Color(255, 236, 164, 90));
        g.fill(new Ellipse2D.Double(bowl.x() + 50, bowl.y() + 18, bowl.w() * 0.5, bowl.h() * 0.42));
        drawHerbLeaves(g, bowl.x() + 420, bowl.y() + 54, 0.65, 6, new Color(74, 136, 73), random);
    }

    private static void renderTofu(Graphics2D g, FoodSpec spec, Random random) {
        BowlShape bowl = drawBowl(g, 250, 310, 700, 290, new Color(239, 235, 228), new Color(154, 131, 107));
        drawLiquidSurface(g, bowl, new Color(219, 184, 122), new Color(177, 129, 76));
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                drawTofuCube(g, bowl.x() + 42 + col * 96, bowl.y() + 24 + row * 54, 58);
            }
        }
        drawHerbLeaves(g, bowl.x() + 420, bowl.y() + 72, 0.9, 8, new Color(70, 132, 78), random);
    }

    private static void renderSteamedPumpkin(Graphics2D g, FoodSpec spec, Random random) {
        PlateShape plate = drawPlate(g, 220, 270, 760, 360, new Color(241, 237, 229), new Color(252, 249, 243));
        for (int i = 0; i < 5; i++) {
            double px = plate.x() + 30 + i * 92;
            double py = plate.y() + 54 + (i % 2) * 22;
            Path2D wedge = new Path2D.Double();
            wedge.moveTo(px, py + 30);
            wedge.curveTo(px + 14, py, px + 80, py, px + 96, py + 30);
            wedge.lineTo(px + 82, py + 104);
            wedge.lineTo(px + 12, py + 104);
            wedge.closePath();
            g.setPaint(new LinearGradientPaint(new Point2D.Double(px, py), new Point2D.Double(px + 96, py + 100),
                    new float[] {0f, 0.72f, 1f},
                    new Color[] {new Color(255, 210, 126), new Color(240, 145, 44), new Color(186, 91, 18)}));
            g.fill(wedge);
        }
    }

    private static void renderRicePorridge(Graphics2D g, FoodSpec spec, Random random) {
        BowlShape bowl = drawBowl(g, 260, 300, 680, 300, new Color(239, 236, 232), new Color(158, 148, 137));
        drawLiquidSurface(g, bowl, new Color(246, 245, 238), new Color(224, 221, 210));
        g.setColor(new Color(255, 255, 255, 90));
        g.fill(new Ellipse2D.Double(bowl.x() + 80, bowl.y() + 24, bowl.w() * 0.4, bowl.h() * 0.30));
        drawSteam(g, 575, 248, 1.05, 3);
    }

    private static void renderYam(Graphics2D g, FoodSpec spec, Random random) {
        PlateShape plate = drawPlate(g, 220, 280, 760, 340, new Color(240, 236, 230), new Color(252, 248, 242));
        drawYamSlices(g, plate.x() + 80, plate.y() + 54, 5, 1.0);
        drawHerbLeaves(g, plate.x() + 440, plate.y() + 34, 0.8, 5, new Color(68, 120, 66), random);
    }

    private static void renderLotusSoup(Graphics2D g, FoodSpec spec, Random random) {
        BowlShape bowl = drawBowl(g, 250, 300, 700, 300, new Color(239, 234, 228), new Color(164, 132, 106));
        drawLiquidSurface(g, bowl, new Color(216, 180, 132), new Color(172, 126, 82));
        for (int i = 0; i < 4; i++) {
            drawLotusSlice(g, bowl.x() + 34 + i * 96, bowl.y() + 20 + (i % 2) * 12, 0.85);
        }
        g.setColor(new Color(124, 88, 65));
        g.fill(new Ellipse2D.Double(bowl.x() + 110, bowl.y() + 88, 66, 42));
        g.fill(new Ellipse2D.Double(bowl.x() + 240, bowl.y() + 96, 74, 46));
        g.fill(new Ellipse2D.Double(bowl.x() + 410, bowl.y() + 86, 70, 44));
    }

    private static void renderRedDate(Graphics2D g, FoodSpec spec, Random random) {
        PlateShape plate = drawPlate(g, 260, 280, 680, 330, new Color(239, 234, 230), new Color(250, 246, 241));
        drawRedDates(g, plate.x() + 110, plate.y() + 70, 6, 1.05, random);
    }

    private static void renderGojiBerry(Graphics2D g, FoodSpec spec, Random random) {
        BoardShape board = drawBoard(g, 250, 270, 700, 360, new Color(144, 96, 62));
        drawGojiPile(g, board.x() + 180, board.y() + 100, 40, 1.2, random);
    }

    private static void renderBlueberry(Graphics2D g, FoodSpec spec, Random random) {
        BowlShape bowl = drawBowl(g, 300, 300, 600, 270, new Color(235, 234, 242), new Color(130, 118, 170));
        drawLiquidSurface(g, bowl, new Color(231, 231, 246), new Color(212, 212, 236));
        drawBlueberries(g, bowl.x() + 100, bowl.y() + 16, 10, 1.25, random);
    }

    private static void renderBroccoli(Graphics2D g, FoodSpec spec, Random random) {
        PlateShape plate = drawPlate(g, 240, 270, 720, 350, new Color(240, 238, 231), new Color(251, 248, 241));
        for (int i = 0; i < 5; i++) {
            drawBroccoliFloret(g, plate.x() + 60 + i * 96, plate.y() + 36 + (i % 2) * 20, 1.0);
        }
    }

    private static void renderKiwi(Graphics2D g, FoodSpec spec, Random random) {
        BoardShape board = drawBoard(g, 210, 260, 780, 380, new Color(150, 112, 84));
        drawKiwiSlice(g, board.x() + 170, board.y() + 120, 84);
        drawKiwiSlice(g, board.x() + 310, board.y() + 80, 80);
        drawKiwiSlice(g, board.x() + 440, board.y() + 134, 72);
        g.setColor(new Color(120, 78, 44));
        g.fill(new Ellipse2D.Double(board.x() + 72, board.y() + 110, 100, 140));
    }

    private static void renderSteamedApple(Graphics2D g, FoodSpec spec, Random random) {
        BowlShape bowl = drawBowl(g, 280, 300, 640, 270, new Color(240, 235, 228), new Color(170, 143, 118));
        drawLiquidSurface(g, bowl, new Color(240, 205, 176), new Color(216, 152, 126));
        drawAppleSlices(g, bowl.x() + 56, bowl.y() + 18, 1.15);
        drawSteam(g, 560, 248, 1.0, 3);
    }

    private static void renderSteamedFish(Graphics2D g, FoodSpec spec, Random random) {
        PlateShape plate = drawPlate(g, 170, 290, 860, 310, new Color(238, 236, 232), new Color(250, 248, 244));
        drawFishBody(g, plate.x() + 90, plate.y() + 74, 1.6);
        drawHerbLeaves(g, plate.x() + 200, plate.y() + 72, 1.2, 10, new Color(80, 152, 92), random);
        scatterSesame(g, plate.x() + 160, plate.y() + 70, plate.w() * 0.6, plate.h() * 0.2, 26, random);
    }

    private static void renderChickenBreast(Graphics2D g, FoodSpec spec, Random random) {
        PlateShape plate = drawPlate(g, 240, 280, 720, 340, new Color(239, 236, 231), new Color(251, 248, 243));
        drawChickenSlices(g, plate.x() + 94, plate.y() + 80, 6, 1.0, random);
        drawHerbLeaves(g, plate.x() + 440, plate.y() + 58, 1.0, 8, new Color(88, 144, 82), random);
    }

    private static void renderYogurt(Graphics2D g, FoodSpec spec, Random random) {
        BowlShape bowl = drawBowl(g, 300, 300, 600, 280, new Color(235, 232, 244), new Color(146, 126, 178));
        drawLiquidSurface(g, bowl, new Color(250, 249, 246), new Color(232, 228, 220));
        drawBlueberries(g, bowl.x() + 140, bowl.y() + 22, 7, 0.95, random);
        drawLemonSlice(g, bowl.x() + 420, bowl.y() + 90, 42);
    }

    private static void renderSoyMilk(Graphics2D g, FoodSpec spec, Random random) {
        drawGlass(g, 410, 170, 300, 510, new Color(234, 220, 173), true);
        for (int i = 0; i < 10; i++) {
            double px = 320 + (i % 5) * 56;
            double py = 620 + (i / 5) * 48;
            g.setPaint(new LinearGradientPaint(new Point2D.Double(px, py), new Point2D.Double(px + 28, py + 22),
                    new float[] {0f, 1f},
                    new Color[] {new Color(250, 239, 188), new Color(216, 191, 110)}));
            g.fill(new Ellipse2D.Double(px, py, 28, 22));
        }
    }

    private static void renderChili(Graphics2D g, FoodSpec spec, Random random) {
        BoardShape board = drawBoard(g, 240, 260, 720, 380, new Color(120, 78, 54));
        drawChilies(g, board.x() + 120, board.y() + 96, 8, 1.15, random);
    }

    private static void renderHotpot(Graphics2D g, FoodSpec spec, Random random) {
        Ellipse2D shadow = new Ellipse2D.Double(260, 610, 680, 120);
        drawShadow(g, shadow, 0, 0, 24);
        Ellipse2D pot = new Ellipse2D.Double(250, 160, 700, 500);
        g.setPaint(new LinearGradientPaint(new Point2D.Double(250, 160), new Point2D.Double(950, 660),
                new float[] {0f, 1f},
                new Color[] {new Color(67, 53, 46), new Color(26, 24, 24)}));
        g.fill(pot);
        g.setColor(new Color(138, 126, 114));
        g.setStroke(new BasicStroke(14f));
        g.draw(pot);
        g.fill(new RoundRectangle2D.Double(216, 332, 48, 150, 18, 18));
        g.fill(new RoundRectangle2D.Double(936, 332, 48, 150, 18, 18));

        Ellipse2D broth = new Ellipse2D.Double(300, 210, 600, 400);
        g.setColor(new Color(68, 48, 36));
        g.fill(broth);
        Path2D split = new Path2D.Double();
        split.moveTo(600, 216);
        split.curveTo(540, 300, 666, 372, 596, 604);
        split.lineTo(618, 604);
        split.curveTo(688, 374, 564, 298, 624, 216);
        split.closePath();
        g.setColor(new Color(220, 212, 196));
        g.fill(split);

        Shape clip = g.getClip();
        g.setClip(broth);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(470, 320), 280f,
                new float[] {0f, 1f},
                new Color[] {new Color(221, 62, 38), new Color(139, 23, 18)}));
        g.fill(new Ellipse2D.Double(300, 210, 300, 400));
        g.setPaint(new RadialGradientPaint(new Point2D.Double(740, 330), 260f,
                new float[] {0f, 1f},
                new Color[] {new Color(182, 148, 88), new Color(118, 92, 54)}));
        g.fill(new Ellipse2D.Double(600, 210, 300, 400));
        drawChilies(g, 380, 260, 6, 0.85, random);
        drawHerbLeaves(g, 700, 260, 1.3, 10, new Color(92, 170, 88), random);
        drawLotusSlice(g, 690, 396, 0.92);
        drawLotusSlice(g, 760, 448, 0.80);
        g.setColor(new Color(126, 72, 54));
        g.fill(new Ellipse2D.Double(640, 300, 86, 56));
        g.fill(new Ellipse2D.Double(748, 330, 94, 60));
        drawBubbles(g, 350, 250, 500, 320, 48, random);
        g.setClip(clip);
        drawSteam(g, 600, 180, 1.4, 4);
    }

    private static void renderWasabi(Graphics2D g, FoodSpec spec, Random random) {
        PlateShape plate = drawPlate(g, 280, 300, 640, 280, new Color(242, 239, 232), new Color(250, 247, 242));
        drawWasabiDollop(g, plate.x() + 240, plate.y() + 46, 2.2);
    }

    private static void renderPeppercorn(Graphics2D g, FoodSpec spec, Random random) {
        BoardShape board = drawBoard(g, 240, 270, 720, 360, new Color(128, 84, 54));
        g.setColor(new Color(168, 114, 64));
        g.fill(new RoundRectangle2D.Double(board.x() + 120, board.y() + 90, 320, 80, 40, 40));
        g.fill(new Ellipse2D.Double(board.x() + 404, board.y() + 74, 120, 112));
        drawPeppercorns(g, board.x() + 170, board.y() + 82, 40, 1.3, random);
    }

    private static void renderFriedChicken(Graphics2D g, FoodSpec spec, Random random) {
        BoardShape board = drawBoard(g, 220, 270, 760, 360, new Color(160, 110, 72));
        g.setColor(new Color(210, 178, 116));
        g.fill(new RoundRectangle2D.Double(board.x() + 150, board.y() + 70, 360, 170, 34, 34));
        for (int i = 0; i < 4; i++) {
            double px = board.x() + 190 + i * 78;
            double py = board.y() + 92 + (i % 2) * 26;
            g.setPaint(new RadialGradientPaint(new Point2D.Double(px + 38, py + 32), 58f,
                    new float[] {0f, 1f},
                    new Color[] {new Color(242, 180, 88), new Color(158, 96, 32)}));
            g.fill(new Ellipse2D.Double(px, py, 96, 74));
        }
        scatterSesame(g, board.x() + 180, board.y() + 80, 300, 120, 28, random);
    }

    private static void renderBbq(Graphics2D g, FoodSpec spec, Random random) {
        BoardShape board = drawBoard(g, 200, 260, 800, 380, new Color(104, 60, 38));
        for (int i = 0; i < 4; i++) {
            double px = board.x() + 90 + i * 90;
            double py = board.y() + 70 + (i % 2) * 26;
            g.setColor(new Color(182, 148, 106));
            g.fill(new RoundRectangle2D.Double(px - 10, py + 34, 240, 10, 6, 6));
            for (int j = 0; j < 4; j++) {
                double mx = px + j * 46;
                g.setPaint(new LinearGradientPaint(new Point2D.Double(mx, py), new Point2D.Double(mx + 32, py + 42),
                        new float[] {0f, 0.65f, 1f},
                        new Color[] {new Color(208, 124, 74), new Color(144, 68, 30), new Color(86, 34, 18)}));
                g.fill(new RoundRectangle2D.Double(mx, py, 34, 44, 10, 10));
            }
        }
    }

    private static void renderFries(Graphics2D g, FoodSpec spec, Random random) {
        BoardShape board = drawBoard(g, 240, 250, 720, 390, new Color(148, 102, 70));
        Path2D box = new Path2D.Double();
        box.moveTo(board.x() + 240, board.y() + 250);
        box.lineTo(board.x() + 460, board.y() + 250);
        box.lineTo(board.x() + 426, board.y() + 70);
        box.lineTo(board.x() + 274, board.y() + 70);
        box.closePath();
        g.setPaint(new LinearGradientPaint(new Point2D.Double(board.x() + 250, board.y() + 70), new Point2D.Double(board.x() + 460, board.y() + 250),
                new float[] {0f, 1f},
                new Color[] {new Color(215, 58, 42), new Color(154, 24, 24)}));
        g.fill(box);
        drawFriesPile(g, board.x() + 270, board.y() + 20, 24, 1.0, random);
    }

    private static void renderFriedDough(Graphics2D g, FoodSpec spec, Random random) {
        PlateShape plate = drawPlate(g, 230, 280, 740, 330, new Color(241, 238, 232), new Color(251, 248, 242));
        for (int i = 0; i < 3; i++) {
            double px = plate.x() + 100 + i * 130;
            double py = plate.y() + 70 + (i % 2) * 20;
            Path2D dough = new Path2D.Double();
            dough.moveTo(px, py + 20);
            dough.curveTo(px + 30, py - 10, px + 110, py - 8, px + 138, py + 26);
            dough.curveTo(px + 108, py + 72, px + 26, py + 74, px, py + 20);
            g.setPaint(new LinearGradientPaint(new Point2D.Double(px, py), new Point2D.Double(px + 140, py + 70),
                    new float[] {0f, 0.65f, 1f},
                    new Color[] {new Color(248, 206, 120), new Color(218, 153, 58), new Color(173, 109, 26)}));
            g.fill(dough);
        }
    }

    private static void renderHotSoup(Graphics2D g, FoodSpec spec, Random random) {
        BowlShape bowl = drawBowl(g, 250, 300, 700, 300, new Color(240, 235, 230), new Color(160, 126, 98));
        drawLiquidSurface(g, bowl, new Color(214, 90, 68), new Color(144, 34, 28));
        drawHerbLeaves(g, bowl.x() + 180, bowl.y() + 66, 1.1, 8, new Color(82, 150, 80), random);
        drawSteam(g, 580, 240, 1.2, 4);
    }

    private static void renderHotTea(Graphics2D g, FoodSpec spec, Random random) {
        drawCup(g, 390, 230, 320, 250, new Color(247, 240, 228), new Color(169, 103, 42));
        drawSteam(g, 550, 220, 1.1, 3);
    }

    private static void renderHotPorridge(Graphics2D g, FoodSpec spec, Random random) {
        BowlShape bowl = drawBowl(g, 260, 300, 680, 300, new Color(239, 234, 228), new Color(176, 145, 108));
        drawLiquidSurface(g, bowl, new Color(236, 202, 118), new Color(190, 144, 60));
        scatterSesame(g, bowl.x() + 30, bowl.y() + 10, bowl.w() - 60, bowl.h() - 20, 56, random);
        drawSteam(g, 575, 246, 1.1, 4);
    }

    private static void renderHotPotHot(Graphics2D g, FoodSpec spec, Random random) {
        renderHotpot(g, spec, random);
        drawSteam(g, 600, 150, 1.7, 5);
        g.setColor(new Color(255, 255, 255, 50));
        g.fill(new Ellipse2D.Double(320, 130, 120, 90));
        g.fill(new Ellipse2D.Double(740, 110, 120, 100));
    }

    private static void renderBaijiu(Graphics2D g, FoodSpec spec, Random random) {
        drawGlass(g, 470, 250, 200, 300, new Color(248, 248, 244, 120), false);
        g.setColor(new Color(255, 255, 255, 38));
        g.fill(new Ellipse2D.Double(440, 590, 320, 60));
    }

    private static void renderCoffee(Graphics2D g, FoodSpec spec, Random random) {
        BowlShape top = drawCup(g, 380, 220, 340, 270, new Color(247, 240, 232), new Color(112, 64, 28));
        g.setColor(new Color(204, 172, 120));
        g.setStroke(new BasicStroke(6f));
        g.draw(new Arc2D.Double(top.x() + 40, top.y() + 6, 150, 70, 200, 140, Arc2D.OPEN));
    }

    private static void renderBeer(Graphics2D g, FoodSpec spec, Random random) {
        drawGlass(g, 430, 150, 290, 540, new Color(228, 170, 40), true);
        g.setColor(new Color(250, 248, 242, 220));
        g.fill(new Ellipse2D.Double(474, 162, 198, 92));
        drawBubbles(g, 460, 250, 180, 330, 46, random);
    }

    private static void renderEnergyDrink(Graphics2D g, FoodSpec spec, Random random) {
        BoardShape board = drawBoard(g, 260, 250, 680, 400, new Color(104, 116, 140));
        RoundRectangle2D can = new RoundRectangle2D.Double(board.x() + 240, board.y() + 20, 160, 280, 30, 30);
        g.setPaint(new LinearGradientPaint(new Point2D.Double(board.x() + 240, board.y() + 20), new Point2D.Double(board.x() + 400, board.y() + 300),
                new float[] {0f, 0.5f, 1f},
                new Color[] {new Color(84, 142, 255), new Color(36, 90, 210), new Color(18, 54, 132)}));
        g.fill(can);
        g.setColor(new Color(255, 255, 255, 44));
        g.fill(new RoundRectangle2D.Double(board.x() + 270, board.y() + 44, 28, 226, 14, 14));
        drawGlass(g, 660, 260, 150, 230, new Color(105, 182, 250), true);
    }

    private static void renderMilkTea(Graphics2D g, FoodSpec spec, Random random) {
        drawGlass(g, 420, 130, 320, 560, new Color(185, 140, 96), true);
        for (int i = 0; i < 11; i++) {
            double px = 480 + (i % 4) * 46;
            double py = 530 + (i / 4) * 40;
            g.setPaint(new RadialGradientPaint(new Point2D.Double(px + 16, py + 16), 18f,
                    new float[] {0f, 1f},
                    new Color[] {new Color(84, 56, 38), new Color(32, 22, 16)}));
            g.fill(new Ellipse2D.Double(px, py, 32, 32));
        }
        g.setColor(new Color(255, 110, 128));
        g.fill(new RoundRectangle2D.Double(606, 90, 16, 190, 8, 8));
    }

    private static void renderChocolate(Graphics2D g, FoodSpec spec, Random random) {
        BoardShape board = drawBoard(g, 240, 270, 720, 360, new Color(118, 78, 52));
        drawChocolatePieces(g, board.x() + 120, board.y() + 80, 1.2, random);
    }

    private static void renderCake(Graphics2D g, FoodSpec spec, Random random) {
        PlateShape plate = drawPlate(g, 250, 280, 700, 340, new Color(241, 236, 231), new Color(251, 248, 242));
        Path2D slice = new Path2D.Double();
        slice.moveTo(plate.x() + 120, plate.y() + 210);
        slice.lineTo(plate.x() + 380, plate.y() + 210);
        slice.lineTo(plate.x() + 250, plate.y() + 56);
        slice.closePath();
        g.setPaint(new LinearGradientPaint(new Point2D.Double(plate.x() + 120, plate.y() + 56), new Point2D.Double(plate.x() + 380, plate.y() + 210),
                new float[] {0f, 0.5f, 1f},
                new Color[] {new Color(255, 226, 230), new Color(242, 188, 214), new Color(188, 86, 126)}));
        g.fill(slice);
        g.setColor(new Color(251, 245, 240));
        g.fill(new RoundRectangle2D.Double(plate.x() + 138, plate.y() + 136, 224, 20, 10, 10));
        g.fill(new RoundRectangle2D.Double(plate.x() + 154, plate.y() + 96, 190, 18, 10, 10));
        drawBlueberries(g, plate.x() + 218, plate.y() + 56, 3, 0.7, random);
    }

    private static void renderIceCream(Graphics2D g, FoodSpec spec, Random random) {
        BowlShape bowl = drawBowl(g, 300, 330, 600, 250, new Color(240, 236, 240), new Color(174, 140, 162));
        g.setPaint(new RadialGradientPaint(new Point2D.Double(472, 350), 86f,
                new float[] {0f, 1f},
                new Color[] {new Color(255, 240, 248), new Color(243, 184, 214)}));
        g.fill(new Ellipse2D.Double(400, 280, 150, 150));
        g.setPaint(new RadialGradientPaint(new Point2D.Double(564, 330), 86f,
                new float[] {0f, 1f},
                new Color[] {new Color(255, 250, 216), new Color(245, 212, 114)}));
        g.fill(new Ellipse2D.Double(500, 260, 150, 150));
        g.setPaint(new RadialGradientPaint(new Point2D.Double(666, 354), 86f,
                new float[] {0f, 1f},
                new Color[] {new Color(234, 245, 210), new Color(170, 210, 120)}));
        g.fill(new Ellipse2D.Double(606, 286, 150, 150));
    }

    private static void renderFallback(Graphics2D g, FoodSpec spec, Random random) {
        PlateShape plate = drawPlate(g, 210, 270, 780, 360, new Color(236, 231, 225), new Color(252, 249, 244));
        g.setColor(new Color(210, 180, 120));
        g.fill(new Ellipse2D.Double(plate.x() + 80, plate.y() + 40, plate.w() - 160, plate.h() - 80));
        drawHerbLeaves(g, plate.x() + 90, plate.y() + 70, 1.2, 6, new Color(76, 132, 72), random);
    }
}
