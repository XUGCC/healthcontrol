import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
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

public class GenerateImageTableFixtureImages {
    private static final int DOCTOR_WIDTH = 960;
    private static final int DOCTOR_HEIGHT = 1280;
    private static final int SCIENCE_WIDTH = 1600;
    private static final int SCIENCE_HEIGHT = 900;
    private static final int LARYNGOSCOPE_WIDTH = 1200;
    private static final int LARYNGOSCOPE_HEIGHT = 900;

    private static final String DOCTOR_URL_PREFIX = "http://localhost:7245/test-fixtures/doctors/";
    private static final String SCIENCE_URL_PREFIX = "http://localhost:7245/test-fixtures/science/";
    private static final String LARYNGOSCOPE_URL_PREFIX = "http://localhost:7245/test-fixtures/laryngoscope/";

    private record DoctorSpec(
            int id,
            String fileName,
            boolean female,
            String accentHex,
            String backgroundHex,
            String secondaryBackgroundHex,
            String skinHex,
            String hairHex,
            int hairStyle,
            int poseVariant,
            boolean glasses
    ) {
        String url() {
            return DOCTOR_URL_PREFIX + fileName;
        }
    }

    private record ScienceSpec(
            int id,
            String fileName,
            String scene,
            String primaryHex,
            String secondaryHex
    ) {
        String url() {
            return SCIENCE_URL_PREFIX + fileName;
        }
    }

    private record LaryngoscopeSpec(
            int id,
            String fileName,
            String scene,
            String primaryHex,
            String secondaryHex
    ) {
        String url() {
            return LARYNGOSCOPE_URL_PREFIX + fileName;
        }
    }

    public static void main(String[] args) throws Exception {
        Path doctorRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of("HealthControl.springboot", "external-resources", "test-fixtures", "doctors").toAbsolutePath();
        Path scienceRoot = args.length > 1
                ? Path.of(args[1]).toAbsolutePath().normalize()
                : Path.of("HealthControl.springboot", "external-resources", "test-fixtures", "science").toAbsolutePath();
        Path laryngoscopeRoot = args.length > 2
                ? Path.of(args[2]).toAbsolutePath().normalize()
                : Path.of("HealthControl.springboot", "external-resources", "test-fixtures", "laryngoscope").toAbsolutePath();
        Path sqlOutput = args.length > 3
                ? Path.of(args[3]).toAbsolutePath().normalize()
                : Path.of("image-table-image-updates.sql").toAbsolutePath();

        Files.createDirectories(doctorRoot);
        Files.createDirectories(scienceRoot);
        Files.createDirectories(laryngoscopeRoot);
        if (sqlOutput.getParent() != null) {
            Files.createDirectories(sqlOutput.getParent());
        }

        List<DoctorSpec> doctors = doctorSpecs();
        List<ScienceSpec> scienceCovers = scienceSpecs();
        List<LaryngoscopeSpec> laryngoscopes = laryngoscopeSpecs();

        for (DoctorSpec spec : doctors) {
            renderDoctor(spec, doctorRoot.resolve(spec.fileName()));
        }
        for (ScienceSpec spec : scienceCovers) {
            renderScienceCover(spec, scienceRoot.resolve(spec.fileName()));
        }
        for (LaryngoscopeSpec spec : laryngoscopes) {
            renderLaryngoscope(spec, laryngoscopeRoot.resolve(spec.fileName()));
        }

        writeSql(sqlOutput, doctors, scienceCovers, laryngoscopes);
        System.out.println("Generated " + doctors.size() + " doctor images to " + doctorRoot);
        System.out.println("Generated " + scienceCovers.size() + " science cover images to " + scienceRoot);
        System.out.println("Generated " + laryngoscopes.size() + " laryngoscope images to " + laryngoscopeRoot);
        System.out.println("Generated SQL file at " + sqlOutput);
    }

    private static List<DoctorSpec> doctorSpecs() {
        return List.of(
                new DoctorSpec(1, "doctor-01-li-chengguang.png", false, "#2F6F9C", "#EAF5FB", "#D4ECFA", "#E7C4A8", "#2B2521", 0, 0, false),
                new DoctorSpec(2, "doctor-02-zhou-yajing.png", true, "#4C8F83", "#F1FBF8", "#DFF5EF", "#EBCBB6", "#392C25", 1, 1, false),
                new DoctorSpec(3, "doctor-03-han-lixin.png", false, "#4F77A4", "#EFF4FD", "#D7E4F8", "#E0BEA5", "#32261F", 2, 0, true),
                new DoctorSpec(4, "doctor-04-chen-jun.png", true, "#7C5EAF", "#F5F1FD", "#E5DBFA", "#EFCBB5", "#2E2420", 3, 2, false),
                new DoctorSpec(5, "doctor-05-wang-ruolan.png", true, "#C96C62", "#FFF4F1", "#F8DED8", "#ECCAB5", "#2B2424", 4, 1, false),
                new DoctorSpec(6, "doctor-06-liu-zhiheng.png", false, "#587C7A", "#EFF7F5", "#D8EBE7", "#DEB89D", "#281E18", 5, 3, true),
                new DoctorSpec(7, "doctor-07-zhang-yue.png", true, "#709063", "#F4FAEF", "#E1F0D4", "#EBCAB7", "#2D2420", 6, 2, true),
                new DoctorSpec(8, "doctor-08-tang-li.png", false, "#C07D3F", "#FFF6EC", "#F9E2C6", "#DFC0A3", "#30251E", 7, 3, false),
                new DoctorSpec(9, "doctor-09-shi-wenqing.png", false, "#8A5C83", "#FBF1FA", "#EDD7EB", "#DEB79F", "#2B1F1E", 8, 0, false),
                new DoctorSpec(10, "doctor-10-jiang-simin.png", true, "#486F9F", "#EEF4FC", "#D8E6F9", "#EFCCB7", "#312824", 9, 1, false)
        );
    }

    private static List<ScienceSpec> scienceSpecs() {
        return List.of(
                new ScienceSpec(2, "throat_food_good.jpg", "food-good", "#E5A354", "#A8D2B2"),
                new ScienceSpec(4, "acute_laryngitis.jpg", "acute-laryngitis", "#7A93C8", "#F4B6A8"),
                new ScienceSpec(5, "vocal_nodules_polyps.jpg", "vocal-nodules", "#688EC3", "#C98EA6"),
                new ScienceSpec(6, "clear_throat.jpg", "clear-throat", "#6D8F8E", "#D9C5A6"),
                new ScienceSpec(7, "voice_warmup_office.jpg", "voice-warmup", "#7C9BD5", "#E2B98E"),
                new ScienceSpec(8, "post_surgery_training.jpg", "post-surgery", "#6E96A9", "#D3A59B"),
                new ScienceSpec(9, "first_visit_ent.jpg", "first-visit", "#7797B6", "#D4C0A7"),
                new ScienceSpec(10, "post_laryngoscopy.jpg", "post-laryngoscopy", "#6A8CA6", "#C2D0DB"),
                new ScienceSpec(11, "overtime_throat.jpg", "overtime-throat", "#2F4B71", "#D38D78"),
                new ScienceSpec(13, "science-test-13.jpg", "generic-throat", "#76A7B8", "#B7D8D2"),
                new ScienceSpec(14, "science-test-14.jpg", "generic-ent", "#7E98BC", "#D0DDC4"),
                new ScienceSpec(15, "science-test-15.jpg", "daily-care", "#88A998", "#E0C6A1"),
                new ScienceSpec(16, "science-test-16.jpg", "medical-qa", "#8398BA", "#D8BFAE"),
                new ScienceSpec(90001, "science-cover-90001.png", "voice-hydration", "#6B96C3", "#D6B07E")
        );
    }

    private static List<LaryngoscopeSpec> laryngoscopeSpecs() {
        return List.of(
                new LaryngoscopeSpec(90001, "laryngoscope-90001.png", "electronic", "#A7474F", "#F2B3B0"),
                new LaryngoscopeSpec(90002, "laryngoscope-90002.png", "fiber", "#8D4F64", "#D7A4A2")
        );
    }

    private static void renderDoctor(DoctorSpec spec, Path target) throws IOException {
        BufferedImage image = new BufferedImage(DOCTOR_WIDTH, DOCTOR_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        enableQuality(g);

        Color accent = color(spec.accentHex());
        Color background = color(spec.backgroundHex());
        Color secondary = color(spec.secondaryBackgroundHex());
        Color skin = color(spec.skinHex());
        Color hair = color(spec.hairHex());
        Random random = new Random(31L * spec.id() + spec.fileName().hashCode());

        drawDoctorBackdrop(g, accent, background, secondary, random);
        drawDoctorPortrait(g, spec, accent, skin, hair, random);
        addDoctorForegroundGlow(g, accent);
        addFilmGrain(g, DOCTOR_WIDTH, DOCTOR_HEIGHT, random, 3600, 1800);
        addVignette(g, DOCTOR_WIDTH, DOCTOR_HEIGHT, 0.58f, 32);

        g.dispose();
        ImageIO.write(image, "png", target.toFile());
    }

    private static void renderScienceCover(ScienceSpec spec, Path target) throws IOException {
        boolean png = spec.fileName().toLowerCase(Locale.ROOT).endsWith(".png");
        BufferedImage image = new BufferedImage(
                SCIENCE_WIDTH,
                SCIENCE_HEIGHT,
                png ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        enableQuality(g);

        Color primary = color(spec.primaryHex());
        Color secondary = color(spec.secondaryHex());
        Random random = new Random(97L * spec.id() + spec.fileName().hashCode());

        drawScienceBackdrop(g, primary, secondary, random);
        drawScienceScene(g, spec, primary, secondary, random);
        addFilmGrain(g, SCIENCE_WIDTH, SCIENCE_HEIGHT, random, 4200, 2000);
        addVignette(g, SCIENCE_WIDTH, SCIENCE_HEIGHT, 0.62f, 26);

        g.dispose();
        ImageIO.write(image, png ? "png" : "jpg", target.toFile());
    }

    private static void renderLaryngoscope(LaryngoscopeSpec spec, Path target) throws IOException {
        BufferedImage image = new BufferedImage(LARYNGOSCOPE_WIDTH, LARYNGOSCOPE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        enableQuality(g);

        Color primary = color(spec.primaryHex());
        Color secondary = color(spec.secondaryHex());
        Random random = new Random(191L * spec.id() + spec.fileName().hashCode());

        drawLaryngoscopeBackdrop(g, primary, secondary);
        drawLaryngealCavity(g, spec, primary, secondary, random);
        addMoistureGlow(g, primary, secondary, random);
        addFilmGrain(g, LARYNGOSCOPE_WIDTH, LARYNGOSCOPE_HEIGHT, random, 3000, 1400);
        addVignette(g, LARYNGOSCOPE_WIDTH, LARYNGOSCOPE_HEIGHT, 0.48f, 54);

        g.dispose();
        ImageIO.write(image, "png", target.toFile());
    }

    private static void writeSql(
            Path sqlOutput,
            List<DoctorSpec> doctors,
            List<ScienceSpec> scienceCovers,
            List<LaryngoscopeSpec> laryngoscopes) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("-- Auto-generated by GenerateImageTableFixtureImages.java");
        lines.add("START TRANSACTION;");
        for (DoctorSpec spec : doctors) {
            lines.add(String.format(Locale.ROOT,
                    "UPDATE `totolaryngologyhospitaldoctor` SET `PicUrl`='%s' WHERE `Id`=%d;",
                    spec.url(), spec.id()));
        }
        for (ScienceSpec spec : scienceCovers) {
            lines.add(String.format(Locale.ROOT,
                    "UPDATE `thealthscience` SET `CoverUrl`='%s' WHERE `Id`=%d;",
                    spec.url(), spec.id()));
        }
        for (LaryngoscopeSpec spec : laryngoscopes) {
            lines.add(String.format(Locale.ROOT,
                    "UPDATE `tlaryngoscopephoto` SET `LaryngoscopePhotoUrl`='%s' WHERE `Id`=%d;",
                    spec.url(), spec.id()));
        }
        lines.add("COMMIT;");
        Files.writeString(sqlOutput, String.join(System.lineSeparator(), lines) + System.lineSeparator(),
                StandardCharsets.UTF_8);
    }

    private static void drawDoctorBackdrop(Graphics2D g, Color accent, Color background, Color secondary, Random random) {
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(0, 0),
                new Point2D.Double(DOCTOR_WIDTH, DOCTOR_HEIGHT),
                new float[] {0f, 0.55f, 1f},
                new Color[] {
                        mix(background, Color.WHITE, 0.12),
                        mix(secondary, Color.WHITE, 0.06),
                        mix(accent, Color.WHITE, 0.78)
                }));
        g.fill(new Rectangle2D.Double(0, 0, DOCTOR_WIDTH, DOCTOR_HEIGHT));

        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(DOCTOR_WIDTH * 0.20, DOCTOR_HEIGHT * 0.18),
                300f,
                new float[] {0f, 1f},
                new Color[] {alpha(Color.WHITE, 110), new Color(255, 255, 255, 0)}));
        g.fill(new Ellipse2D.Double(10, 40, 500, 420));

        g.setColor(alpha(mix(accent, Color.WHITE, 0.76), 46));
        for (int i = 0; i < 8; i++) {
            double x = 86 + i * 96;
            g.fill(new RoundRectangle2D.Double(x, 118, 64, 176, 24, 24));
        }
        g.setColor(alpha(new Color(255, 255, 255), 120));
        for (int i = 0; i < 8; i++) {
            double x = 96 + i * 96;
            g.fill(new RoundRectangle2D.Double(x, 130, 44, 152, 16, 16));
        }

        for (int i = 0; i < 4; i++) {
            double x = 90 + i * 184;
            double y = 360 + (i % 2) * 110;
            drawShadow(g, new RoundRectangle2D.Double(x, y, 170, 128, 28, 28), 0, 8, 18);
            g.setPaint(new GradientPaint((float) x, (float) y, alpha(Color.WHITE, 196),
                    (float) (x + 170), (float) (y + 128), alpha(mix(accent, secondary, 0.5), 92)));
            g.fill(new RoundRectangle2D.Double(x, y, 170, 128, 28, 28));
            g.setColor(alpha(mix(accent, Color.WHITE, 0.36), 90));
            g.fill(new RoundRectangle2D.Double(x + 18, y + 24, 90, 14, 7, 7));
            g.fill(new RoundRectangle2D.Double(x + 18, y + 54, 122, 12, 6, 6));
            g.fill(new RoundRectangle2D.Double(x + 18, y + 80, 106, 12, 6, 6));
        }

        g.setPaint(new GradientPaint(0, 760, alpha(mix(secondary, accent, 0.18), 0),
                0, DOCTOR_HEIGHT, alpha(mix(accent, Color.BLACK, 0.55), 72)));
        g.fill(new Rectangle2D.Double(0, 720, DOCTOR_WIDTH, 560));

        for (int i = 0; i < 12; i++) {
            double cx = random.nextDouble() * DOCTOR_WIDTH;
            double cy = random.nextDouble() * DOCTOR_HEIGHT * 0.7;
            double radius = 70 + random.nextDouble() * 110;
            g.setPaint(new RadialGradientPaint(
                    new Point2D.Double(cx, cy),
                    (float) radius,
                    new float[] {0f, 1f},
                    new Color[] {
                            alpha(mix(accent, Color.WHITE, 0.25 + random.nextDouble() * 0.30), 20 + random.nextInt(20)),
                            new Color(0, 0, 0, 0)
                    }));
            g.fill(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));
        }

        drawClinicLamp(g, 730, 230, accent);
    }

    private static void drawDoctorPortrait(Graphics2D g, DoctorSpec spec, Color accent, Color skin, Color hair, Random random) {
        double centerX = DOCTOR_WIDTH / 2.0;
        double torsoY = 470;
        double coatTop = 520;

        Path2D torsoShadow = new Path2D.Double();
        torsoShadow.moveTo(centerX - 246, torsoY + 54);
        torsoShadow.curveTo(centerX - 312, torsoY + 168, centerX - 288, torsoY + 436, centerX - 210, DOCTOR_HEIGHT - 70);
        torsoShadow.lineTo(centerX + 208, DOCTOR_HEIGHT - 70);
        torsoShadow.curveTo(centerX + 292, torsoY + 438, centerX + 318, torsoY + 166, centerX + 250, torsoY + 54);
        torsoShadow.closePath();
        drawShadow(g, torsoShadow, 0, 28, 32);

        Path2D coat = new Path2D.Double();
        coat.moveTo(centerX - 230, coatTop);
        coat.curveTo(centerX - 286, coatTop + 144, centerX - 286, coatTop + 402, centerX - 198, DOCTOR_HEIGHT - 52);
        coat.lineTo(centerX + 198, DOCTOR_HEIGHT - 52);
        coat.curveTo(centerX + 284, coatTop + 398, centerX + 284, coatTop + 144, centerX + 230, coatTop);
        coat.curveTo(centerX + 154, coatTop - 32, centerX - 154, coatTop - 32, centerX - 230, coatTop);
        coat.closePath();
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(centerX - 260, coatTop),
                new Point2D.Double(centerX + 260, DOCTOR_HEIGHT - 40),
                new float[] {0f, 0.45f, 1f},
                new Color[] {new Color(255, 255, 255), new Color(244, 248, 252), new Color(223, 232, 239)}));
        g.fill(coat);

        drawDoctorLapels(g, centerX, coatTop, accent);
        drawDoctorShirt(g, centerX, coatTop, accent, spec.poseVariant());
        drawDoctorArms(g, centerX, coatTop, accent, skin, spec.poseVariant(), random);

        g.setColor(new Color(214, 226, 235));
        g.setStroke(new BasicStroke(4.5f));
        g.draw(new CubicCurve2D.Double(centerX - 140, coatTop + 70, centerX - 180, coatTop + 170, centerX - 172, coatTop + 370, centerX - 128, coatTop + 546));
        g.draw(new CubicCurve2D.Double(centerX + 140, coatTop + 70, centerX + 180, coatTop + 170, centerX + 172, coatTop + 370, centerX + 128, coatTop + 546));

        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(centerX - 52, 360),
                new Point2D.Double(centerX + 52, 520),
                new float[] {0f, 1f},
                new Color[] {mix(skin, Color.WHITE, 0.10), mix(skin, new Color(166, 120, 96), 0.25)}));
        g.fill(new RoundRectangle2D.Double(centerX - 42, 370, 84, 104, 40, 40));

        Ellipse2D face = new Ellipse2D.Double(centerX - 132, 216, 264, 332);
        drawShadow(g, face, 0, 18, 20);
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(centerX - 18, 282),
                220f,
                new float[] {0f, 0.7f, 1f},
                new Color[] {mix(skin, Color.WHITE, 0.16), skin, mix(skin, new Color(152, 104, 85), 0.24)}));
        g.fill(face);

        drawDoctorHair(g, spec, centerX, hair);
        drawDoctorFeatures(g, spec, centerX, skin, hair);
        drawDoctorAccessories(g, spec, centerX, accent);

        g.setColor(alpha(new Color(255, 255, 255), 72));
        g.fill(new Ellipse2D.Double(centerX - 84, 250, 60, 132));
        g.setColor(alpha(new Color(244, 164, 160), 28));
        g.fill(new Ellipse2D.Double(centerX - 114, 380, 46, 26));
        g.fill(new Ellipse2D.Double(centerX + 66, 380, 46, 26));
    }

    private static void addDoctorForegroundGlow(Graphics2D g, Color accent) {
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(0, 900),
                new Point2D.Double(0, DOCTOR_HEIGHT),
                new float[] {0f, 1f},
                new Color[] {new Color(255, 255, 255, 0), alpha(mix(accent, Color.WHITE, 0.65), 54)}));
        g.fill(new Rectangle2D.Double(0, 850, DOCTOR_WIDTH, 430));
    }

    private static void drawDoctorLapels(Graphics2D g, double centerX, double coatTop, Color accent) {
        Path2D leftLap = new Path2D.Double();
        leftLap.moveTo(centerX - 112, coatTop - 10);
        leftLap.lineTo(centerX - 18, coatTop + 218);
        leftLap.lineTo(centerX - 112, coatTop + 198);
        leftLap.lineTo(centerX - 168, coatTop + 16);
        leftLap.closePath();
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(centerX - 168, coatTop),
                new Point2D.Double(centerX - 18, coatTop + 218),
                new float[] {0f, 1f},
                new Color[] {new Color(255, 255, 255), new Color(232, 241, 247)}));
        g.fill(leftLap);

        Path2D rightLap = new Path2D.Double();
        rightLap.moveTo(centerX + 112, coatTop - 10);
        rightLap.lineTo(centerX + 18, coatTop + 218);
        rightLap.lineTo(centerX + 112, coatTop + 198);
        rightLap.lineTo(centerX + 168, coatTop + 16);
        rightLap.closePath();
        g.fill(rightLap);

        g.setColor(alpha(accent, 40));
        g.setStroke(new BasicStroke(3.2f));
        g.draw(leftLap);
        g.draw(rightLap);
    }

    private static void drawDoctorShirt(Graphics2D g, double centerX, double coatTop, Color accent, int poseVariant) {
        Color scrubs = mix(accent, new Color(38, 78, 112), 0.35);
        Path2D shirt = new Path2D.Double();
        shirt.moveTo(centerX - 92, coatTop - 6);
        shirt.curveTo(centerX - 52, coatTop + 82, centerX - 36, coatTop + 170, centerX, coatTop + 232);
        shirt.curveTo(centerX + 36, coatTop + 170, centerX + 52, coatTop + 82, centerX + 92, coatTop - 6);
        shirt.lineTo(centerX + 138, coatTop + 164);
        shirt.curveTo(centerX + 62, coatTop + 224, centerX + 40, coatTop + 318, centerX + 32, coatTop + 510);
        shirt.lineTo(centerX - 32, coatTop + 510);
        shirt.curveTo(centerX - 40, coatTop + 318, centerX - 62, coatTop + 224, centerX - 138, coatTop + 164);
        shirt.closePath();
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(centerX, coatTop - 6),
                new Point2D.Double(centerX, coatTop + 510),
                new float[] {0f, 1f},
                new Color[] {mix(scrubs, Color.WHITE, 0.12), mix(scrubs, Color.BLACK, 0.16)}));
        g.fill(shirt);

        if (poseVariant == 1 || poseVariant == 3) {
            g.setColor(alpha(Color.WHITE, 32));
            g.fill(new RoundRectangle2D.Double(centerX - 12, coatTop + 88, 24, 150, 12, 12));
        }
    }

    private static void drawDoctorArms(Graphics2D g, double centerX, double coatTop, Color accent, Color skin, int poseVariant, Random random) {
        Color sleeve = new Color(246, 249, 251);
        if (poseVariant == 0) {
            Path2D leftArm = new Path2D.Double();
            leftArm.moveTo(centerX - 198, coatTop + 98);
            leftArm.curveTo(centerX - 314, coatTop + 210, centerX - 280, coatTop + 402, centerX - 156, coatTop + 428);
            leftArm.curveTo(centerX - 92, coatTop + 434, centerX - 64, coatTop + 412, centerX - 44, coatTop + 384);
            leftArm.curveTo(centerX - 128, coatTop + 374, centerX - 204, coatTop + 278, centerX - 198, coatTop + 98);
            leftArm.closePath();
            g.setPaint(new GradientPaint((float) (centerX - 198), (float) (coatTop + 98), sleeve,
                    (float) (centerX - 48), (float) (coatTop + 420), new Color(223, 232, 240)));
            g.fill(leftArm);
            g.fill(mirror(leftArm, centerX));
        } else if (poseVariant == 1) {
            Path2D leftArm = new Path2D.Double();
            leftArm.moveTo(centerX - 198, coatTop + 102);
            leftArm.curveTo(centerX - 274, coatTop + 174, centerX - 250, coatTop + 314, centerX - 128, coatTop + 376);
            leftArm.curveTo(centerX - 80, coatTop + 364, centerX - 46, coatTop + 336, centerX - 28, coatTop + 302);
            leftArm.curveTo(centerX - 118, coatTop + 284, centerX - 174, coatTop + 214, centerX - 198, coatTop + 102);
            leftArm.closePath();
            g.setPaint(new GradientPaint((float) (centerX - 198), (float) (coatTop + 102), sleeve,
                    (float) (centerX - 26), (float) (coatTop + 376), new Color(220, 230, 238)));
            g.fill(leftArm);

            Path2D rightArm = new Path2D.Double();
            rightArm.moveTo(centerX + 192, coatTop + 126);
            rightArm.curveTo(centerX + 302, coatTop + 226, centerX + 310, coatTop + 446, centerX + 240, coatTop + 596);
            rightArm.curveTo(centerX + 190, coatTop + 606, centerX + 146, coatTop + 588, centerX + 118, coatTop + 544);
            rightArm.curveTo(centerX + 198, coatTop + 472, centerX + 218, coatTop + 310, centerX + 192, coatTop + 126);
            rightArm.closePath();
            g.fill(rightArm);
            drawHand(g, centerX + 104, coatTop + 532, skin, random, false);
            drawClipboard(g, centerX + 120, coatTop + 438, accent);
        } else if (poseVariant == 2) {
            Path2D leftArm = new Path2D.Double();
            leftArm.moveTo(centerX - 202, coatTop + 126);
            leftArm.curveTo(centerX - 324, coatTop + 276, centerX - 284, coatTop + 462, centerX - 144, coatTop + 544);
            leftArm.curveTo(centerX - 104, coatTop + 534, centerX - 72, coatTop + 516, centerX - 54, coatTop + 488);
            leftArm.curveTo(centerX - 152, coatTop + 442, centerX - 212, coatTop + 328, centerX - 202, coatTop + 126);
            leftArm.closePath();
            g.setPaint(new GradientPaint((float) (centerX - 202), (float) (coatTop + 126), sleeve,
                    (float) (centerX - 52), (float) (coatTop + 540), new Color(219, 229, 237)));
            g.fill(leftArm);

            Path2D rightArm = new Path2D.Double();
            rightArm.moveTo(centerX + 202, coatTop + 96);
            rightArm.curveTo(centerX + 282, coatTop + 162, centerX + 260, coatTop + 296, centerX + 176, coatTop + 362);
            rightArm.curveTo(centerX + 122, coatTop + 328, centerX + 90, coatTop + 282, centerX + 72, coatTop + 236);
            rightArm.curveTo(centerX + 146, coatTop + 220, centerX + 196, coatTop + 160, centerX + 202, coatTop + 96);
            rightArm.closePath();
            g.fill(rightArm);
            drawHand(g, centerX - 50, coatTop + 474, skin, random, true);
            drawHand(g, centerX + 66, coatTop + 228, skin, random, false);
        } else {
            Path2D leftArm = new Path2D.Double();
            leftArm.moveTo(centerX - 204, coatTop + 118);
            leftArm.curveTo(centerX - 320, coatTop + 232, centerX - 290, coatTop + 438, centerX - 160, coatTop + 522);
            leftArm.curveTo(centerX - 118, coatTop + 514, centerX - 86, coatTop + 494, centerX - 66, coatTop + 462);
            leftArm.curveTo(centerX - 154, coatTop + 414, centerX - 210, coatTop + 316, centerX - 204, coatTop + 118);
            leftArm.closePath();
            g.setPaint(new GradientPaint((float) (centerX - 204), (float) (coatTop + 118), sleeve,
                    (float) (centerX - 62), (float) (coatTop + 522), new Color(221, 230, 238)));
            g.fill(leftArm);

            Path2D rightArm = new Path2D.Double();
            rightArm.moveTo(centerX + 192, coatTop + 112);
            rightArm.curveTo(centerX + 314, coatTop + 244, centerX + 286, coatTop + 474, centerX + 124, coatTop + 612);
            rightArm.curveTo(centerX + 80, coatTop + 602, centerX + 44, coatTop + 578, centerX + 26, coatTop + 544);
            rightArm.curveTo(centerX + 122, coatTop + 470, centerX + 224, coatTop + 318, centerX + 192, coatTop + 112);
            rightArm.closePath();
            g.fill(rightArm);
            drawHand(g, centerX - 62, coatTop + 450, skin, random, true);
            drawHand(g, centerX + 16, coatTop + 532, skin, random, false);
            drawStethoscope(g, centerX, coatTop + 102, accent);
        }
    }

    private static void drawDoctorHair(Graphics2D g, DoctorSpec spec, double centerX, Color hair) {
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(centerX - 120, 186),
                new Point2D.Double(centerX + 120, 448),
                new float[] {0f, 0.75f, 1f},
                new Color[] {mix(hair, Color.WHITE, 0.08), hair, mix(hair, Color.BLACK, 0.22)}));

        Path2D p = new Path2D.Double();
        switch (spec.hairStyle()) {
            case 0 -> {
                p.moveTo(centerX - 124, 308);
                p.curveTo(centerX - 150, 180, centerX - 34, 128, centerX + 104, 168);
                p.curveTo(centerX + 150, 192, centerX + 146, 292, centerX + 114, 344);
                p.curveTo(centerX + 70, 258, centerX - 18, 238, centerX - 90, 260);
                p.curveTo(centerX - 114, 276, centerX - 126, 292, centerX - 124, 308);
            }
            case 1 -> {
                p.moveTo(centerX - 136, 248);
                p.curveTo(centerX - 114, 134, centerX + 14, 122, centerX + 124, 176);
                p.curveTo(centerX + 164, 206, centerX + 158, 354, centerX + 116, 432);
                p.curveTo(centerX + 82, 494, centerX + 20, 524, centerX - 34, 518);
                p.curveTo(centerX - 104, 510, centerX - 152, 442, centerX - 162, 360);
                p.curveTo(centerX - 170, 310, centerX - 156, 270, centerX - 136, 248);
            }
            case 2 -> {
                p.moveTo(centerX - 122, 250);
                p.curveTo(centerX - 118, 154, centerX - 4, 118, centerX + 116, 170);
                p.curveTo(centerX + 150, 194, centerX + 154, 262, centerX + 132, 328);
                p.curveTo(centerX + 96, 260, centerX + 10, 220, centerX - 68, 238);
                p.curveTo(centerX - 98, 248, centerX - 114, 254, centerX - 122, 250);
            }
            case 3 -> {
                p.moveTo(centerX - 126, 242);
                p.curveTo(centerX - 118, 150, centerX + 8, 130, centerX + 122, 176);
                p.curveTo(centerX + 166, 214, centerX + 172, 366, centerX + 136, 462);
                p.curveTo(centerX + 82, 408, centerX - 84, 416, centerX - 136, 466);
                p.curveTo(centerX - 160, 396, centerX - 168, 294, centerX - 126, 242);
            }
            case 4 -> {
                p.moveTo(centerX - 136, 246);
                p.curveTo(centerX - 108, 142, centerX + 20, 128, centerX + 124, 180);
                p.curveTo(centerX + 160, 210, centerX + 166, 350, centerX + 126, 430);
                p.curveTo(centerX + 104, 476, centerX + 88, 486, centerX + 64, 496);
                p.curveTo(centerX + 72, 430, centerX + 28, 390, centerX - 24, 392);
                p.curveTo(centerX - 74, 394, centerX - 122, 432, centerX - 146, 486);
                p.curveTo(centerX - 164, 404, centerX - 166, 300, centerX - 136, 246);
            }
            case 5 -> {
                p.moveTo(centerX - 128, 258);
                p.curveTo(centerX - 136, 162, centerX - 18, 126, centerX + 110, 172);
                p.curveTo(centerX + 152, 198, centerX + 154, 292, centerX + 118, 350);
                p.curveTo(centerX + 74, 292, centerX - 4, 268, centerX - 92, 286);
                p.curveTo(centerX - 114, 292, centerX - 124, 282, centerX - 128, 258);
            }
            case 6 -> {
                p.moveTo(centerX - 128, 252);
                p.curveTo(centerX - 116, 144, centerX + 12, 126, centerX + 126, 182);
                p.curveTo(centerX + 162, 214, centerX + 166, 362, centerX + 126, 444);
                p.curveTo(centerX + 76, 426, centerX + 34, 416, centerX + 2, 416);
                p.curveTo(centerX - 38, 416, centerX - 88, 430, centerX - 142, 458);
                p.curveTo(centerX - 162, 390, centerX - 166, 298, centerX - 128, 252);
            }
            case 7 -> {
                p.moveTo(centerX - 126, 260);
                p.curveTo(centerX - 144, 178, centerX - 34, 124, centerX + 106, 166);
                p.curveTo(centerX + 152, 188, centerX + 148, 284, centerX + 116, 344);
                p.curveTo(centerX + 60, 288, centerX - 28, 262, centerX - 92, 278);
                p.curveTo(centerX - 114, 284, centerX - 124, 282, centerX - 126, 260);
            }
            case 8 -> {
                p.moveTo(centerX - 118, 252);
                p.curveTo(centerX - 126, 154, centerX - 12, 126, centerX + 112, 172);
                p.curveTo(centerX + 152, 194, centerX + 158, 286, centerX + 134, 350);
                p.curveTo(centerX + 88, 286, centerX + 8, 246, centerX - 74, 252);
                p.curveTo(centerX - 96, 256, centerX - 112, 256, centerX - 118, 252);
            }
            default -> {
                p.moveTo(centerX - 130, 246);
                p.curveTo(centerX - 114, 146, centerX + 10, 126, centerX + 122, 180);
                p.curveTo(centerX + 160, 212, centerX + 164, 350, centerX + 126, 438);
                p.curveTo(centerX + 78, 412, centerX + 34, 404, centerX - 4, 406);
                p.curveTo(centerX - 48, 410, centerX - 92, 424, centerX - 144, 456);
                p.curveTo(centerX - 164, 390, centerX - 166, 294, centerX - 130, 246);
            }
        }
        p.closePath();
        g.fill(p);
    }

    private static void drawDoctorFeatures(Graphics2D g, DoctorSpec spec, double centerX, Color skin, Color hair) {
        g.setColor(alpha(mix(hair, Color.BLACK, 0.22), 200));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(centerX - 84, 320, 56, 22, 15, 150, Arc2D.OPEN));
        g.draw(new Arc2D.Double(centerX + 28, 320, 56, 22, 15, 150, Arc2D.OPEN));

        g.setColor(new Color(65, 56, 52));
        g.fill(new Ellipse2D.Double(centerX - 68, 348, 18, 12));
        g.fill(new Ellipse2D.Double(centerX + 50, 348, 18, 12));
        g.setColor(alpha(Color.WHITE, 180));
        g.fill(new Ellipse2D.Double(centerX - 62, 350, 6, 4));
        g.fill(new Ellipse2D.Double(centerX + 56, 350, 6, 4));

        g.setColor(alpha(mix(skin, new Color(118, 78, 62), 0.54), 160));
        g.setStroke(new BasicStroke(4.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new CubicCurve2D.Double(centerX, 352, centerX - 4, 382, centerX - 10, 406, centerX + 8, 430));
        g.setColor(alpha(mix(skin, new Color(176, 124, 108), 0.50), 170));
        g.draw(new Arc2D.Double(centerX - 28, 450, 56, 28, 200, 140, Arc2D.OPEN));

        g.setColor(alpha(new Color(181, 116, 104), 72));
        g.fill(new Ellipse2D.Double(centerX - 110, 392, 24, 38));
        g.fill(new Ellipse2D.Double(centerX + 86, 392, 24, 38));

        if (spec.glasses()) {
            g.setColor(alpha(new Color(72, 88, 108), 190));
            g.setStroke(new BasicStroke(5f));
            g.draw(new RoundRectangle2D.Double(centerX - 88, 336, 62, 44, 16, 16));
            g.draw(new RoundRectangle2D.Double(centerX + 26, 336, 62, 44, 16, 16));
            g.draw(new Arc2D.Double(centerX - 12, 350, 24, 10, 0, 180, Arc2D.OPEN));
        }
    }

    private static void drawDoctorAccessories(Graphics2D g, DoctorSpec spec, double centerX, Color accent) {
        if (spec.poseVariant() != 3) {
            drawStethoscope(g, centerX, 608, accent);
        }

        RoundRectangle2D badge = new RoundRectangle2D.Double(centerX + 126, 676, 72, 104, 18, 18);
        g.setColor(new Color(250, 251, 252, 226));
        g.fill(badge);
        g.setColor(alpha(accent, 116));
        g.fill(new RoundRectangle2D.Double(centerX + 126, 676, 72, 24, 18, 18));
        g.setColor(alpha(new Color(176, 188, 199), 140));
        g.fill(new RoundRectangle2D.Double(centerX + 140, 716, 44, 12, 6, 6));
        g.fill(new RoundRectangle2D.Double(centerX + 140, 742, 34, 10, 5, 5));
        g.fill(new Ellipse2D.Double(centerX + 147, 764, 26, 26));
    }

    private static Path2D mirror(Path2D path, double centerX) {
        AffineTransform transform = AffineTransform.getScaleInstance(-1, 1);
        transform.translate(-2 * centerX, 0);
        return (Path2D) path.createTransformedShape(transform);
    }

    private static void drawClinicLamp(Graphics2D g, double x, double y, Color accent) {
        g.setColor(alpha(mix(accent, Color.WHITE, 0.56), 86));
        g.setStroke(new BasicStroke(8f));
        g.draw(new Arc2D.Double(x - 160, y - 160, 260, 260, 330, 124, Arc2D.OPEN));
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(x, y),
                90f,
                new float[] {0f, 1f},
                new Color[] {alpha(Color.WHITE, 190), alpha(mix(accent, Color.WHITE, 0.70), 0)}));
        g.fill(new Ellipse2D.Double(x - 90, y - 90, 180, 180));
    }

    private static void drawHand(Graphics2D g, double x, double y, Color skin, Random random, boolean left) {
        double dir = left ? -1 : 1;
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x + 50 * dir, y + 48),
                new float[] {0f, 1f},
                new Color[] {mix(skin, Color.WHITE, 0.06), mix(skin, new Color(168, 126, 106), 0.18)}));
        g.fill(new RoundRectangle2D.Double(x - 18, y - 12, 52, 40, 18, 18));
        for (int i = 0; i < 4; i++) {
            g.fill(new RoundRectangle2D.Double(x + i * 10 * dir, y - 30 + random.nextInt(5), 14, 28, 8, 8));
        }
    }

    private static void drawClipboard(Graphics2D g, double x, double y, Color accent) {
        g.setColor(new Color(248, 247, 244, 228));
        g.fill(new RoundRectangle2D.Double(x, y, 104, 148, 18, 18));
        g.setColor(alpha(accent, 110));
        g.fill(new RoundRectangle2D.Double(x + 30, y - 8, 44, 18, 9, 9));
        g.setColor(alpha(new Color(194, 198, 206), 110));
        for (int i = 0; i < 6; i++) {
            g.fill(new RoundRectangle2D.Double(x + 18, y + 26 + i * 18, 56, 8, 4, 4));
        }
    }

    private static void drawStethoscope(Graphics2D g, double centerX, double y, Color accent) {
        g.setColor(alpha(mix(accent, new Color(80, 92, 112), 0.40), 170));
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new CubicCurve2D.Double(centerX - 92, y, centerX - 118, y + 76, centerX - 92, y + 138, centerX - 44, y + 178));
        g.draw(new CubicCurve2D.Double(centerX + 92, y, centerX + 118, y + 76, centerX + 92, y + 138, centerX + 44, y + 178));
        g.draw(new CubicCurve2D.Double(centerX - 44, y + 178, centerX - 24, y + 216, centerX + 24, y + 216, centerX + 44, y + 178));
        g.fill(new Ellipse2D.Double(centerX - 112, y - 12, 28, 28));
        g.fill(new Ellipse2D.Double(centerX + 84, y - 12, 28, 28));
        g.fill(new Ellipse2D.Double(centerX - 24, y + 192, 48, 48));
    }

    private static void drawSceneCard(Graphics2D g, double x, double y, double w, double h, Color primary, Color secondary) {
        drawShadow(g, new RoundRectangle2D.Double(x, y, w, h, 56, 56), 0, 22, 30);
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x + w, y + h),
                new float[] {0f, 0.5f, 1f},
                new Color[] {
                        new Color(255, 255, 255, 232),
                        mix(secondary, Color.WHITE, 0.60),
                        mix(primary, Color.WHITE, 0.72)
                }));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 56, 56));
        g.setColor(alpha(mix(primary, secondary, 0.5), 70));
        g.setStroke(new BasicStroke(4f));
        g.draw(new RoundRectangle2D.Double(x + 6, y + 6, w - 12, h - 12, 52, 52));
    }

    private static void drawRoomCard(Graphics2D g, Color primary, Color secondary) {
        drawSceneCard(g, 100, 110, 1400, 680, primary, secondary);
        drawWindowPanel(g, 180, 150, 360, 220, primary);
    }

    private static void drawClinicCard(Graphics2D g, Color primary, Color secondary) {
        drawSceneCard(g, 92, 118, 1416, 664, primary, secondary);
        drawWindowPanel(g, 144, 144, 360, 220, primary);
    }

    private static void drawOfficeCard(Graphics2D g, Color primary, Color secondary) {
        drawSceneCard(g, 110, 120, 1380, 660, primary, secondary);
        drawWindowPanel(g, 156, 146, 360, 210, primary);
    }

    private static void drawWindowPanel(Graphics2D g, double x, double y, double w, double h, Color accent) {
        drawShadow(g, new RoundRectangle2D.Double(x, y, w, h, 30, 30), 0, 10, 18);
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x, y + h),
                new float[] {0f, 1f},
                new Color[] {alpha(Color.WHITE, 220), alpha(mix(accent, Color.WHITE, 0.72), 150)}));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 30, 30));
        g.setColor(alpha(Color.WHITE, 160));
        g.fill(new Rectangle2D.Double(x + w * 0.48, y + 20, 12, h - 40));
        g.fill(new Rectangle2D.Double(x + 20, y + h * 0.48, w - 40, 12));
    }

    private static void drawCup(Graphics2D g, double x, double y, double w, double h, Color porcelain, Color liquid, boolean steam) {
        drawShadow(g, new Ellipse2D.Double(x + w * 0.14, y + h * 0.92, w * 0.72, h * 0.12), 0, 8, 18);
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x + w, y + h),
                new float[] {0f, 1f},
                new Color[] {porcelain, mix(porcelain, new Color(210, 215, 220), 0.20)}));
        g.fill(new RoundRectangle2D.Double(x + w * 0.16, y + h * 0.14, w * 0.58, h * 0.68, 32, 32));
        g.setColor(alpha(Color.WHITE, 80));
        g.fill(new RoundRectangle2D.Double(x + w * 0.24, y + h * 0.20, w * 0.14, h * 0.48, 18, 18));
        g.setStroke(new BasicStroke(8f));
        g.setColor(mix(porcelain, new Color(198, 206, 214), 0.28));
        g.draw(new Arc2D.Double(x + w * 0.52, y + h * 0.28, w * 0.34, h * 0.30, 300, 246, Arc2D.OPEN));
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(x + w * 0.42, y + h * 0.18),
                (float) (w * 0.22),
                new float[] {0f, 1f},
                new Color[] {mix(liquid, Color.WHITE, 0.22), mix(liquid, Color.BLACK, 0.10)}));
        g.fill(new Ellipse2D.Double(x + w * 0.22, y + h * 0.08, w * 0.38, h * 0.12));
        if (steam) {
            drawSteam(g, x + w * 0.38, y - 16, 0.72, 3);
        }
    }

    private static void drawPearPlate(Graphics2D g, double x, double y, double w, double h) {
        g.setColor(new Color(244, 239, 230));
        g.fill(new Ellipse2D.Double(x, y + h * 0.38, w, h * 0.40));
        g.setColor(new Color(250, 246, 241));
        g.fill(new Ellipse2D.Double(x + w * 0.10, y + h * 0.40, w * 0.80, h * 0.26));
        drawThroatPear(g, x + w * 0.22, y + h * 0.12, w * 0.28, h * 0.34, new Color(188, 216, 128));
        drawThroatPear(g, x + w * 0.54, y + h * 0.16, w * 0.22, h * 0.28, new Color(208, 226, 152));
    }

    private static void drawThroatPear(Graphics2D g, double x, double y, double w, double h, Color pear) {
        Path2D pearShape = new Path2D.Double();
        pearShape.moveTo(x + w * 0.5, y);
        pearShape.curveTo(x + w * 0.10, y + h * 0.06, x - w * 0.10, y + h * 0.62, x + w * 0.5, y + h);
        pearShape.curveTo(x + w * 1.10, y + h * 0.62, x + w * 0.90, y + h * 0.06, x + w * 0.5, y);
        pearShape.closePath();
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x + w, y + h),
                new float[] {0f, 1f},
                new Color[] {mix(pear, Color.WHITE, 0.16), mix(pear, new Color(132, 164, 72), 0.24)}));
        g.fill(pearShape);
        g.setColor(new Color(248, 242, 218, 190));
        g.fill(new Ellipse2D.Double(x + w * 0.16, y + h * 0.20, w * 0.68, h * 0.58));
        g.setColor(new Color(174, 132, 96, 170));
        g.fill(new Ellipse2D.Double(x + w * 0.44, y + h * 0.44, w * 0.10, h * 0.12));
    }

    private static void drawJar(Graphics2D g, double x, double y, double w, double h, Color lid, Color honey) {
        drawShadow(g, new Ellipse2D.Double(x + w * 0.16, y + h * 0.88, w * 0.68, h * 0.14), 0, 8, 20);
        g.setColor(lid);
        g.fill(new RoundRectangle2D.Double(x + w * 0.18, y + h * 0.08, w * 0.64, h * 0.16, 22, 22));
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x + w * 0.18, y + h * 0.22),
                new Point2D.Double(x + w * 0.82, y + h * 0.80),
                new float[] {0f, 1f},
                new Color[] {alpha(Color.WHITE, 120), alpha(honey, 220)}));
        g.fill(new RoundRectangle2D.Double(x + w * 0.20, y + h * 0.20, w * 0.60, h * 0.56, 42, 42));
    }

    private static void drawBowl(Graphics2D g, double x, double y, double w, double h, Color bowl, Color food) {
        g.setColor(mix(bowl, new Color(220, 210, 192), 0.12));
        g.fill(new Ellipse2D.Double(x, y + h * 0.40, w, h * 0.40));
        g.setColor(bowl);
        g.fill(new Ellipse2D.Double(x + w * 0.10, y + h * 0.44, w * 0.80, h * 0.26));
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(x + w * 0.42, y + h * 0.34),
                (float) (w * 0.24),
                new float[] {0f, 1f},
                new Color[] {mix(food, Color.WHITE, 0.18), mix(food, Color.BLACK, 0.10)}));
        g.fill(new Ellipse2D.Double(x + w * 0.18, y + h * 0.20, w * 0.64, h * 0.22));
    }

    private static void drawLeafSprigs(Graphics2D g, double x, double y, Color leaf, Random random) {
        for (int i = 0; i < 6; i++) {
            double px = x + Math.cos(i * 0.8) * 28 + random.nextDouble() * 12 - 6;
            double py = y + Math.sin(i * 0.9) * 20 + random.nextDouble() * 10 - 5;
            Path2D p = new Path2D.Double();
            p.moveTo(px, py);
            p.curveTo(px - 16, py - 10, px - 20, py + 22, px, py + 32);
            p.curveTo(px + 18, py + 22, px + 16, py - 10, px, py);
            p.closePath();
            g.setPaint(new LinearGradientPaint(
                    new Point2D.Double(px, py),
                    new Point2D.Double(px, py + 32),
                    new float[] {0f, 1f},
                    new Color[] {mix(leaf, Color.WHITE, 0.16), mix(leaf, Color.BLACK, 0.14)}));
            g.fill(p);
        }
    }

    private static void drawSofa(Graphics2D g, double x, double y, double w, double h, Color upholstery) {
        g.setColor(upholstery);
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 46, 46));
        g.setColor(alpha(Color.WHITE, 56));
        g.fill(new RoundRectangle2D.Double(x + 36, y + 34, w * 0.28, h * 0.44, 30, 30));
        g.fill(new RoundRectangle2D.Double(x + w * 0.39, y + 34, w * 0.28, h * 0.44, 30, 30));
    }

    private static void drawSimpleCharacter(Graphics2D g, double x, double y, double scale, boolean female, Color clothing, boolean touchingThroat, boolean raisedHand) {
        drawShadow(g, new Ellipse2D.Double(x - 150 * scale, y + 160 * scale, 300 * scale, 54 * scale), 0, 8, 24);
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y - 80 * scale),
                new Point2D.Double(x, y + 220 * scale),
                new float[] {0f, 1f},
                new Color[] {mix(clothing, Color.WHITE, 0.12), mix(clothing, Color.BLACK, 0.18)}));
        Path2D body = new Path2D.Double();
        body.moveTo(x - 116 * scale, y - 34 * scale);
        body.curveTo(x - 148 * scale, y + 40 * scale, x - 138 * scale, y + 110 * scale, x - 96 * scale, y + 184 * scale);
        body.lineTo(x + 96 * scale, y + 184 * scale);
        body.curveTo(x + 138 * scale, y + 110 * scale, x + 148 * scale, y + 40 * scale, x + 116 * scale, y - 34 * scale);
        body.curveTo(x + 70 * scale, y - 60 * scale, x - 70 * scale, y - 60 * scale, x - 116 * scale, y - 34 * scale);
        body.closePath();
        g.fill(body);
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(x, y - 170 * scale),
                (float) (120 * scale),
                new float[] {0f, 1f},
                new Color[] {new Color(242, 208, 186), new Color(214, 172, 146)}));
        g.fill(new Ellipse2D.Double(x - 72 * scale, y - 266 * scale, 144 * scale, 186 * scale));
        g.setColor(new Color(58, 44, 40));
        g.fill(createHairSilhouette(x, y - 168 * scale, 84 * scale, female));
        g.setColor(new Color(66, 52, 48));
        g.fill(new Ellipse2D.Double(x - 34 * scale, y - 168 * scale, 16 * scale, 10 * scale));
        g.fill(new Ellipse2D.Double(x + 18 * scale, y - 168 * scale, 16 * scale, 10 * scale));
        g.setStroke(new BasicStroke((float) (3.2 * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(x - 18 * scale, y - 120 * scale, 36 * scale, 18 * scale, 200, 140, Arc2D.OPEN));
        if (touchingThroat) {
            g.setColor(new Color(232, 198, 180));
            g.fill(new RoundRectangle2D.Double(x + 24 * scale, y - 8 * scale, 72 * scale, 112 * scale, 24, 24));
        }
        if (raisedHand) {
            g.setColor(new Color(232, 198, 180));
            g.fill(new RoundRectangle2D.Double(x + 62 * scale, y - 52 * scale, 72 * scale, 116 * scale, 24, 24));
        }
    }

    private static Shape createHairSilhouette(double x, double y, double radius, boolean female) {
        Path2D hair = new Path2D.Double();
        if (female) {
            hair.moveTo(x - radius * 1.05, y + 12);
            hair.curveTo(x - radius * 0.96, y - radius * 0.76, x + radius * 0.22, y - radius * 0.86, x + radius * 1.02, y - radius * 0.22);
            hair.curveTo(x + radius * 1.12, y + radius * 0.16, x + radius * 0.96, y + radius * 1.24, x + radius * 0.46, y + radius * 1.66);
            hair.curveTo(x + radius * 0.12, y + radius * 1.18, x - radius * 0.12, y + radius * 1.06, x - radius * 0.42, y + radius * 1.62);
            hair.curveTo(x - radius * 0.92, y + radius * 1.14, x - radius * 1.10, y + radius * 0.34, x - radius * 1.05, y + 12);
        } else {
            hair.moveTo(x - radius * 0.98, y + 6);
            hair.curveTo(x - radius * 1.06, y - radius * 0.62, x - radius * 0.20, y - radius * 0.96, x + radius * 0.82, y - radius * 0.52);
            hair.curveTo(x + radius * 1.08, y - radius * 0.22, x + radius * 1.04, y + radius * 0.34, x + radius * 0.78, y + radius * 0.88);
            hair.curveTo(x + radius * 0.20, y + radius * 0.48, x - radius * 0.30, y + radius * 0.40, x - radius * 0.92, y + radius * 0.70);
            hair.curveTo(x - radius * 0.98, y + radius * 0.42, x - radius * 1.02, y + radius * 0.20, x - radius * 0.98, y + 6);
        }
        hair.closePath();
        return hair;
    }

    private static void drawSteam(Graphics2D g, double x, double y, double scale, int lines) {
        g.setStroke(new BasicStroke((float) (4.0 * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < lines; i++) {
            double offset = (i - (lines - 1) / 2.0) * 18 * scale;
            g.setColor(new Color(255, 255, 255, 120 - i * 10));
            g.draw(new CubicCurve2D.Double(
                    x + offset,
                    y + 86 * scale,
                    x - 16 * scale + offset,
                    y + 50 * scale,
                    x + 18 * scale + offset,
                    y + 26 * scale,
                    x + offset,
                    y));
        }
    }

    private static void drawTissueBox(Graphics2D g, double x, double y, Color accent) {
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x + 120, y + 80),
                new float[] {0f, 1f},
                new Color[] {new Color(248, 245, 239), mix(accent, Color.WHITE, 0.56)}));
        g.fill(new RoundRectangle2D.Double(x, y, 126, 80, 18, 18));
        g.setColor(alpha(new Color(255, 255, 255), 190));
        g.fill(new Ellipse2D.Double(x + 32, y - 12, 60, 36));
    }

    private static void drawGlow(Graphics2D g, double x, double y, Color accent) {
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(x, y),
                120f,
                new float[] {0f, 1f},
                new Color[] {alpha(mix(accent, Color.WHITE, 0.34), 120), new Color(255, 255, 255, 0)}));
        g.fill(new Ellipse2D.Double(x - 120, y - 120, 240, 240));
    }

    private static void drawDoctorFigure(Graphics2D g, double x, double y, double scale, Color accent, boolean female) {
        drawSimpleCharacter(g, x, y, scale, female, mix(accent, Color.WHITE, 0.14), false, false);
        g.setColor(new Color(240, 245, 248, 210));
        g.fill(new RoundRectangle2D.Double(x - 112 * scale, y - 30 * scale, 224 * scale, 260 * scale, 32, 32));
        g.setColor(alpha(accent, 42));
        g.setStroke(new BasicStroke(3f));
        g.draw(new RoundRectangle2D.Double(x - 112 * scale, y - 30 * scale, 224 * scale, 260 * scale, 32, 32));
    }

    private static void drawMonitor(Graphics2D g, double x, double y, double w, double h, Color casing) {
        g.setColor(casing);
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 24, 24));
        g.setColor(new Color(24, 36, 48));
        g.fill(new RoundRectangle2D.Double(x + 16, y + 16, w - 32, h - 32, 16, 16));
        g.setColor(alpha(Color.WHITE, 18));
        g.fill(new Rectangle2D.Double(x + 30, y + 26, w - 60, h - 46));
        g.fill(new Rectangle2D.Double(x + w * 0.46, y + h, w * 0.08, 52));
        g.fill(new RoundRectangle2D.Double(x + w * 0.30, y + h + 44, w * 0.40, 16, 8, 8));
    }

    private static void drawThroatIcon(Graphics2D g, double x, double y, double scale, Color primary, Color secondary) {
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(x, y),
                (float) (180 * scale),
                new float[] {0f, 1f},
                new Color[] {alpha(mix(primary, secondary, 0.42), 160), alpha(mix(primary, secondary, 0.42), 0)}));
        g.fill(new Ellipse2D.Double(x - 180 * scale, y - 180 * scale, 360 * scale, 360 * scale));
        Path2D badge = new Path2D.Double();
        badge.moveTo(x, y - 100 * scale);
        badge.curveTo(x - 84 * scale, y - 54 * scale, x - 96 * scale, y + 32 * scale, x - 44 * scale, y + 120 * scale);
        badge.curveTo(x - 16 * scale, y + 164 * scale, x + 16 * scale, y + 164 * scale, x + 44 * scale, y + 120 * scale);
        badge.curveTo(x + 96 * scale, y + 32 * scale, x + 84 * scale, y - 54 * scale, x, y - 100 * scale);
        badge.closePath();
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y - 100 * scale),
                new Point2D.Double(x, y + 140 * scale),
                new float[] {0f, 1f},
                new Color[] {mix(secondary, Color.WHITE, 0.16), mix(primary, new Color(150, 78, 90), 0.38)}));
        g.fill(badge);
        g.setColor(new Color(56, 18, 28, 170));
        g.fill(new Ellipse2D.Double(x - 16 * scale, y + 8 * scale, 32 * scale, 98 * scale));
    }

    private static void drawMagnifier(Graphics2D g, double x, double y, double scale, Color accent) {
        g.setStroke(new BasicStroke((float) (14 * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(alpha(Color.WHITE, 180));
        g.draw(new Ellipse2D.Double(x - 62 * scale, y - 62 * scale, 124 * scale, 124 * scale));
        g.setColor(alpha(accent, 190));
        g.draw(new RoundRectangle2D.Double(x + 42 * scale, y + 52 * scale, 86 * scale, 18 * scale, 12, 12));
    }

    private static void drawDesk(Graphics2D g, double x, double y, double w, double h, Color wood) {
        drawShadow(g, new RoundRectangle2D.Double(x, y, w, h, 34, 34), 0, 12, 18);
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x, y + h),
                new float[] {0f, 1f},
                new Color[] {mix(wood, Color.WHITE, 0.16), mix(wood, Color.BLACK, 0.12)}));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 34, 34));
    }

    private static void drawLaptop(Graphics2D g, double x, double y, double w, double h, Color casing) {
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x + w, y + h),
                new float[] {0f, 1f},
                new Color[] {mix(casing, Color.WHITE, 0.12), mix(casing, Color.BLACK, 0.16)}));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 26, 26));
        g.setColor(new Color(20, 34, 48));
        g.fill(new RoundRectangle2D.Double(x + 18, y + 18, w - 36, h - 36, 18, 18));
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(x + w * 0.42, y + h * 0.42),
                (float) (w * 0.34),
                new float[] {0f, 1f},
                new Color[] {alpha(new Color(144, 196, 255), 180), new Color(0, 0, 0, 0)}));
        g.fill(new Ellipse2D.Double(x + 40, y + 26, w - 80, h - 60));
        g.setColor(alpha(Color.WHITE, 38));
        g.fill(new RoundRectangle2D.Double(x - 24, y + h - 6, w + 48, 18, 12, 12));
    }

    private static void drawSoundRings(Graphics2D g, double x, double y, Color accent) {
        for (int i = 0; i < 4; i++) {
            g.setColor(alpha(mix(accent, Color.WHITE, 0.26 + i * 0.10), 120 - i * 20));
            g.setStroke(new BasicStroke(4f));
            g.draw(new Ellipse2D.Double(x + i * 24, y - i * 18, 90 + i * 44, 90 + i * 44));
        }
    }

    private static void drawChair(Graphics2D g, double x, double y, double w, double h, Color upholstery) {
        g.setColor(upholstery);
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 40, 40));
        g.fill(new RoundRectangle2D.Double(x + 28, y + h * 0.62, w - 56, h * 0.24, 24, 24));
    }

    private static void drawGuideWave(Graphics2D g, double x, double y, Color primary, Color secondary) {
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(alpha(mix(primary, secondary, 0.38), 150));
        g.draw(new CubicCurve2D.Double(x - 130, y + 56, x - 40, y - 22, x + 44, y - 22, x + 128, y + 56));
        g.draw(new CubicCurve2D.Double(x - 106, y + 92, x - 30, y + 22, x + 26, y + 22, x + 104, y + 92));
    }

    private static void drawDocument(Graphics2D g, double x, double y, double w, double h, Color accent) {
        drawShadow(g, new RoundRectangle2D.Double(x, y, w, h, 28, 28), 0, 12, 18);
        g.setColor(new Color(250, 249, 246));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 28, 28));
        g.setColor(alpha(accent, 66));
        g.fill(new RoundRectangle2D.Double(x + 28, y + 36, w - 56, 18, 9, 9));
        for (int i = 0; i < 8; i++) {
            g.setColor(alpha(new Color(190, 196, 204), 120));
            g.fill(new RoundRectangle2D.Double(x + 30, y + 82 + i * 38, w - 60, 12, 6, 6));
        }
        g.fill(new Ellipse2D.Double(x + w - 96, y + h - 116, 54, 54));
    }

    private static void drawPhone(Graphics2D g, double x, double y, double w, double h, Color body) {
        drawShadow(g, new RoundRectangle2D.Double(x, y, w, h, 34, 34), 0, 12, 18);
        g.setColor(body);
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 34, 34));
        g.setColor(new Color(14, 22, 30));
        g.fill(new RoundRectangle2D.Double(x + 10, y + 18, w - 20, h - 36, 26, 26));
        g.setColor(alpha(new Color(126, 180, 226), 150));
        g.fill(new RoundRectangle2D.Double(x + 24, y + 40, w - 48, h * 0.44, 22, 22));
        for (int i = 0; i < 4; i++) {
            g.setColor(alpha(Color.WHITE, 52));
            g.fill(new RoundRectangle2D.Double(x + 24, y + 220 + i * 36, w - 48, 18, 9, 9));
        }
    }

    private static void drawFolder(Graphics2D g, double x, double y, double w, double h, Color paper) {
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x + w, y + h),
                new float[] {0f, 1f},
                new Color[] {mix(paper, Color.WHITE, 0.16), mix(paper, Color.BLACK, 0.10)}));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 26, 26));
        g.setColor(alpha(Color.WHITE, 48));
        g.fill(new RoundRectangle2D.Double(x + 18, y + 18, w * 0.30, 18, 9, 9));
    }

    private static void drawPen(Graphics2D g, double x, double y, Color body) {
        AffineTransform old = g.getTransform();
        g.rotate(Math.toRadians(18), x, y);
        g.setColor(body);
        g.fill(new RoundRectangle2D.Double(x, y, 18, 160, 9, 9));
        g.setColor(new Color(224, 212, 184));
        g.fill(new RoundRectangle2D.Double(x, y + 132, 18, 28, 9, 9));
        g.setTransform(old);
    }

    private static void drawTray(Graphics2D g, double x, double y, double w, double h, Color tray) {
        g.setColor(tray);
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 34, 34));
        g.setColor(alpha(Color.WHITE, 42));
        g.fill(new RoundRectangle2D.Double(x + 22, y + 22, w - 44, h - 44, 24, 24));
        g.fill(new RoundRectangle2D.Double(x + 122, y + 64, 140, 42, 16, 16));
        g.fill(new RoundRectangle2D.Double(x + 308, y + 58, 180, 54, 18, 18));
        g.fill(new RoundRectangle2D.Double(x + 560, y + 62, 240, 48, 16, 16));
    }

    private static void drawScope(Graphics2D g, double x, double y, Color accent) {
        AffineTransform old = g.getTransform();
        g.rotate(Math.toRadians(-18), x, y);
        g.setColor(mix(accent, new Color(58, 70, 86), 0.36));
        g.fill(new RoundRectangle2D.Double(x, y, 220, 28, 12, 12));
        g.fill(new RoundRectangle2D.Double(x + 164, y - 42, 60, 110, 16, 16));
        g.setColor(new Color(214, 222, 228));
        g.fill(new RoundRectangle2D.Double(x + 208, y - 18, 86, 12, 6, 6));
        g.setTransform(old);
    }

    private static void drawSoftPack(Graphics2D g, double x, double y, Color accent) {
        g.setColor(new Color(250, 250, 249, 220));
        g.fill(new RoundRectangle2D.Double(x, y, 160, 92, 22, 22));
        g.setColor(alpha(accent, 74));
        g.fill(new Ellipse2D.Double(x + 56, y + 24, 48, 42));
    }

    private static void drawWindowNight(Graphics2D g, double x, double y, double w, double h) {
        g.setColor(new Color(18, 30, 54, 210));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 24, 24));
        g.setColor(alpha(new Color(118, 164, 228), 120));
        g.fill(new Rectangle2D.Double(x + 28, y + 20, 8, h - 40));
        g.fill(new Rectangle2D.Double(x + w * 0.48, y + 20, 8, h - 40));
        g.fill(new Rectangle2D.Double(x + 20, y + h * 0.48, w - 40, 8));
    }

    private static void drawHumidifier(Graphics2D g, double x, double y, Color accent) {
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x, y + 140),
                new float[] {0f, 1f},
                new Color[] {new Color(248, 249, 250), mix(accent, Color.WHITE, 0.54)}));
        g.fill(new RoundRectangle2D.Double(x, y + 24, 110, 112, 28, 28));
        g.setColor(alpha(accent, 100));
        g.fill(new Ellipse2D.Double(x + 34, y + 10, 42, 18));
        drawSteam(g, x + 52, y - 20, 0.56, 3);
    }

    private static void drawNotebook(Graphics2D g, double x, double y, Color accent) {
        g.setColor(new Color(247, 245, 242, 220));
        g.fill(new RoundRectangle2D.Double(x, y, 220, 152, 22, 22));
        g.setColor(alpha(accent, 96));
        for (int i = 0; i < 6; i++) {
            g.fill(new RoundRectangle2D.Double(x + 26, y + 30 + i * 18, 130, 8, 4, 4));
        }
        g.setColor(alpha(new Color(194, 184, 172), 110));
        g.fill(new Rectangle2D.Double(x + 172, y + 26, 10, 100));
    }

    private static void drawQuestionClouds(Graphics2D g, double x, double y, Color primary, Color secondary) {
        g.setColor(alpha(mix(primary, Color.WHITE, 0.34), 180));
        g.fill(new RoundRectangle2D.Double(x, y, 118, 86, 30, 30));
        g.fill(new RoundRectangle2D.Double(x + 132, y + 40, 98, 76, 28, 28));
        g.setColor(alpha(mix(secondary, Color.WHITE, 0.26), 160));
        g.fill(new Ellipse2D.Double(x + 30, y + 104, 18, 18));
        g.fill(new Ellipse2D.Double(x + 176, y + 126, 16, 16));
        g.setColor(alpha(Color.WHITE, 82));
        g.fill(new RoundRectangle2D.Double(x + 24, y + 28, 44, 10, 5, 5));
        g.fill(new RoundRectangle2D.Double(x + 24, y + 48, 30, 10, 5, 5));
        g.fill(new RoundRectangle2D.Double(x + 156, y + 68, 34, 10, 5, 5));
    }

    private static void drawLaryngoscopeBackdrop(Graphics2D g, Color primary, Color secondary) {
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(LARYNGOSCOPE_WIDTH / 2.0, LARYNGOSCOPE_HEIGHT * 0.48),
                640f,
                new float[] {0f, 0.65f, 1f},
                new Color[] {
                        mix(secondary, new Color(255, 228, 226), 0.26),
                        mix(primary, new Color(70, 24, 30), 0.42),
                        new Color(18, 6, 10)
                }));
        g.fill(new Rectangle2D.Double(0, 0, LARYNGOSCOPE_WIDTH, LARYNGOSCOPE_HEIGHT));
    }

    private static void drawLaryngealCavity(Graphics2D g, LaryngoscopeSpec spec, Color primary, Color secondary, Random random) {
        double cx = LARYNGOSCOPE_WIDTH / 2.0;
        double cy = LARYNGOSCOPE_HEIGHT * 0.5;
        Shape leftWall = createLarynxWall(cx - 30, cy, true, "fiber".equals(spec.scene()));
        Shape rightWall = createLarynxWall(cx + 30, cy, false, "fiber".equals(spec.scene()));
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(240, 120),
                new Point2D.Double(560, 760),
                new float[] {0f, 0.5f, 1f},
                new Color[] {mix(secondary, Color.WHITE, 0.06), primary, mix(primary, new Color(92, 20, 28), 0.44)}));
        g.fill(leftWall);
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(960, 120),
                new Point2D.Double(640, 760),
                new float[] {0f, 0.5f, 1f},
                new Color[] {mix(secondary, Color.WHITE, 0.06), primary, mix(primary, new Color(92, 20, 28), 0.44)}));
        g.fill(rightWall);

        Path2D folds = new Path2D.Double();
        folds.moveTo(470, 334);
        folds.curveTo(522, 360, 544, 482, 532, 666);
        folds.curveTo(500, 650, 474, 566, 454, 442);
        folds.curveTo(446, 394, 452, 354, 470, 334);
        folds.closePath();
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(470, 334),
                new Point2D.Double(540, 666),
                new float[] {0f, 0.55f, 1f},
                new Color[] {new Color(255, 221, 208), new Color(214, 156, 150), new Color(150, 72, 84)}));
        g.fill(folds);
        g.fill(mirror(folds, cx));

        g.setColor(new Color(20, 0, 6));
        Path2D slit = new Path2D.Double();
        slit.moveTo(584, 332);
        slit.curveTo(562, 392, 556, 512, 580, 676);
        slit.curveTo(602, 690, 618, 690, 638, 676);
        slit.curveTo(662, 512, 656, 392, 634, 332);
        slit.curveTo(618, 324, 600, 324, 584, 332);
        slit.closePath();
        g.fill(slit);

        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 12; i++) {
            double startX = 304 + random.nextDouble() * 160;
            double endX = 740 + random.nextDouble() * 150;
            double startY = 214 + i * 34 + random.nextDouble() * 12;
            double ctrlY = cy + (random.nextDouble() - 0.5) * 50;
            g.setColor(alpha(mix(primary, new Color(122, 32, 42), 0.34), 72));
            g.draw(new CubicCurve2D.Double(startX, startY, cx - 90, ctrlY, cx + 90, ctrlY, endX, startY));
        }
    }

    private static void addMoistureGlow(Graphics2D g, Color primary, Color secondary, Random random) {
        for (int i = 0; i < 16; i++) {
            double x = 260 + random.nextDouble() * 680;
            double y = 160 + random.nextDouble() * 500;
            double r = 14 + random.nextDouble() * 34;
            g.setPaint(new RadialGradientPaint(
                    new Point2D.Double(x, y),
                    (float) r,
                    new float[] {0f, 1f},
                    new Color[] {alpha(mix(secondary, Color.WHITE, 0.18), 60), new Color(255, 255, 255, 0)}));
            g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        }
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(LARYNGOSCOPE_WIDTH / 2.0, 210),
                330f,
                new float[] {0f, 1f},
                new Color[] {alpha(mix(secondary, Color.WHITE, 0.20), 94), new Color(255, 255, 255, 0)}));
        g.fill(new Ellipse2D.Double(250, 0, 700, 520));
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(0, 0),
                new Point2D.Double(0, LARYNGOSCOPE_HEIGHT),
                new float[] {0f, 1f},
                new Color[] {new Color(255, 255, 255, 0), alpha(mix(primary, Color.BLACK, 0.35), 40)}));
        g.fill(new Rectangle2D.Double(0, 0, LARYNGOSCOPE_WIDTH, LARYNGOSCOPE_HEIGHT));
    }

    private static Shape createLarynxWall(double x, double y, boolean left, boolean fiber) {
        Path2D wall = new Path2D.Double();
        double dir = left ? -1 : 1;
        wall.moveTo(x + dir * 244, 196);
        wall.curveTo(x + dir * 332, 250, x + dir * 332, 654, x + dir * 224, 726);
        wall.curveTo(x + dir * 128, 782, x + dir * 44, 726, x + dir * 46, 640);
        wall.curveTo(x + dir * 52, 528, x + dir * 82, 410, x + dir * 118, 306);
        wall.curveTo(x + dir * 142, 236, x + dir * 166, 210, x + dir * 244, 196);
        if (fiber) {
            wall.curveTo(x + dir * 204, 256, x + dir * 174, 402, x + dir * 178, 588);
            wall.curveTo(x + dir * 178, 678, x + dir * 200, 732, x + dir * 244, 742);
        }
        wall.closePath();
        return wall;
    }

    private static void drawScienceBackdrop(Graphics2D g, Color primary, Color secondary, Random random) {
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(0, 0),
                new Point2D.Double(SCIENCE_WIDTH, SCIENCE_HEIGHT),
                new float[] {0f, 0.55f, 1f},
                new Color[] {
                        mix(primary, Color.WHITE, 0.78),
                        mix(secondary, Color.WHITE, 0.68),
                        mix(primary, Color.WHITE, 0.88)
                }));
        g.fill(new Rectangle2D.Double(0, 0, SCIENCE_WIDTH, SCIENCE_HEIGHT));

        g.setColor(alpha(Color.WHITE, 120));
        g.fill(new Ellipse2D.Double(50, 20, 540, 360));
        g.fill(new Ellipse2D.Double(1080, 40, 430, 300));

        for (int i = 0; i < 16; i++) {
            double cx = random.nextDouble() * SCIENCE_WIDTH;
            double cy = random.nextDouble() * SCIENCE_HEIGHT;
            double radius = 70 + random.nextDouble() * 170;
            g.setPaint(new RadialGradientPaint(
                    new Point2D.Double(cx, cy),
                    (float) radius,
                    new float[] {0f, 1f},
                    new Color[] {alpha(mix(primary, secondary, random.nextDouble()), 18 + random.nextInt(14)), new Color(0, 0, 0, 0)}));
            g.fill(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));
        }
    }

    private static void drawScienceScene(Graphics2D g, ScienceSpec spec, Color primary, Color secondary, Random random) {
        switch (spec.scene()) {
            case "food-good" -> renderScienceFoodGood(g, primary, secondary, random);
            case "acute-laryngitis" -> renderScienceAcuteLaryngitis(g, primary, secondary, random);
            case "vocal-nodules" -> renderScienceVocalNodules(g, primary, secondary, random);
            case "clear-throat" -> renderScienceClearThroat(g, primary, secondary, random);
            case "voice-warmup" -> renderScienceVoiceWarmup(g, primary, secondary, random);
            case "post-surgery" -> renderSciencePostSurgery(g, primary, secondary, random);
            case "first-visit" -> renderScienceFirstVisit(g, primary, secondary, random);
            case "post-laryngoscopy" -> renderSciencePostLaryngoscopy(g, primary, secondary, random);
            case "overtime-throat" -> renderScienceOvertime(g, primary, secondary, random);
            case "generic-throat" -> renderScienceGenericThroat(g, primary, secondary, random);
            case "generic-ent" -> renderScienceGenericEnt(g, primary, secondary, random);
            case "daily-care" -> renderScienceDailyCare(g, primary, secondary, random);
            case "medical-qa" -> renderScienceMedicalQa(g, primary, secondary, random);
            case "voice-hydration" -> renderScienceVoiceHydration(g, primary, secondary, random);
            default -> renderScienceGenericThroat(g, primary, secondary, random);
        }
    }

    private static void renderScienceFoodGood(Graphics2D g, Color primary, Color secondary, Random random) {
        drawSceneCard(g, 110, 120, 1380, 660, primary, secondary);
        drawCup(g, 280, 408, 188, 212, new Color(245, 238, 228), new Color(205, 165, 96), true);
        drawPearPlate(g, 546, 410, 288, 222);
        drawJar(g, 948, 402, 176, 232, new Color(184, 128, 56), new Color(248, 215, 120));
        drawBowl(g, 1182, 442, 240, 160, new Color(243, 233, 208), new Color(236, 202, 120));
        drawLeafSprigs(g, 236, 208, new Color(108, 162, 104), random);
        drawLeafSprigs(g, 1312, 236, new Color(118, 168, 114), random);
    }

    private static void renderScienceAcuteLaryngitis(Graphics2D g, Color primary, Color secondary, Random random) {
        drawRoomCard(g, primary, secondary);
        drawSofa(g, 220, 500, 520, 188, mix(secondary, new Color(170, 160, 152), 0.24));
        drawSimpleCharacter(g, 760, 604, 1.18, false, new Color(118, 142, 194), true, false);
        drawCup(g, 1090, 428, 110, 128, new Color(246, 240, 232), new Color(206, 168, 98), true);
        drawTissueBox(g, 1120, 560, primary);
        drawGlow(g, 806, 498, primary);
    }

    private static void renderScienceVocalNodules(Graphics2D g, Color primary, Color secondary, Random random) {
        drawClinicCard(g, primary, secondary);
        drawDoctorFigure(g, 444, 620, 1.02, primary, false);
        drawMonitor(g, 960, 210, 388, 260, new Color(48, 68, 92));
        drawThroatIcon(g, 1144, 336, 1.08, primary, secondary);
        drawMagnifier(g, 886, 466, 1.0, primary);
    }

    private static void renderScienceClearThroat(Graphics2D g, Color primary, Color secondary, Random random) {
        drawOfficeCard(g, primary, secondary);
        drawDesk(g, 214, 616, 1180, 144, mix(secondary, new Color(146, 118, 88), 0.34));
        drawSimpleCharacter(g, 708, 604, 1.18, false, new Color(94, 128, 138), true, false);
        drawLaptop(g, 1002, 446, 270, 176, new Color(62, 82, 104));
        drawCup(g, 346, 450, 104, 122, new Color(246, 240, 232), new Color(194, 156, 90), true);
        drawGlow(g, 742, 506, primary);
    }

    private static void renderScienceVoiceWarmup(Graphics2D g, Color primary, Color secondary, Random random) {
        drawOfficeCard(g, primary, secondary);
        drawDesk(g, 214, 616, 1180, 144, mix(secondary, new Color(146, 118, 88), 0.34));
        drawSimpleCharacter(g, 632, 598, 1.12, true, primary, false, true);
        drawLaptop(g, 1018, 446, 270, 176, new Color(62, 82, 104));
        drawCup(g, 344, 448, 110, 124, new Color(246, 240, 232), new Color(195, 156, 90), true);
        drawSoundRings(g, 768, 332, primary);
    }

    private static void renderSciencePostSurgery(Graphics2D g, Color primary, Color secondary, Random random) {
        drawClinicCard(g, primary, secondary);
        drawDoctorFigure(g, 414, 620, 0.96, primary, true);
        drawSimpleCharacter(g, 1038, 626, 0.94, false, new Color(164, 144, 180), false, false);
        drawChair(g, 884, 548, 260, 184, mix(secondary, new Color(158, 140, 132), 0.24));
        drawGuideWave(g, 748, 328, primary, secondary);
    }

    private static void renderScienceFirstVisit(Graphics2D g, Color primary, Color secondary, Random random) {
        drawSceneCard(g, 120, 126, 1360, 648, primary, secondary);
        drawDocument(g, 292, 210, 320, 414, primary);
        drawDocument(g, 626, 248, 304, 382, secondary);
        drawPhone(g, 1072, 248, 160, 292, new Color(52, 64, 82));
        drawFolder(g, 1038, 556, 264, 138, mix(primary, new Color(128, 154, 184), 0.32));
        drawPen(g, 1310, 238, new Color(88, 110, 152));
        drawMagnifier(g, 876, 560, 0.78, primary);
    }

    private static void renderSciencePostLaryngoscopy(Graphics2D g, Color primary, Color secondary, Random random) {
        drawClinicCard(g, primary, secondary);
        drawTray(g, 242, 472, 1108, 198, mix(primary, new Color(194, 202, 212), 0.34));
        drawCup(g, 378, 346, 118, 134, new Color(246, 240, 232), new Color(210, 176, 104), true);
        drawScope(g, 904, 416, primary);
        drawSoftPack(g, 628, 398, secondary);
        drawGlow(g, 434, 324, primary);
    }

    private static void renderScienceOvertime(Graphics2D g, Color primary, Color secondary, Random random) {
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(0, 0),
                new Point2D.Double(0, SCIENCE_HEIGHT),
                new float[] {0f, 1f},
                new Color[] {new Color(23, 38, 62), new Color(7, 18, 28)}));
        g.fill(new Rectangle2D.Double(0, 0, SCIENCE_WIDTH, SCIENCE_HEIGHT));
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(1160, 250),
                260f,
                new float[] {0f, 1f},
                new Color[] {alpha(new Color(126, 182, 255), 150), new Color(0, 0, 0, 0)}));
        g.fill(new Ellipse2D.Double(860, 40, 520, 420));
        drawDesk(g, 150, 610, 1300, 150, new Color(70, 64, 62));
        drawLaptop(g, 980, 408, 290, 200, new Color(58, 72, 104));
        drawSimpleCharacter(g, 622, 614, 1.20, false, new Color(80, 102, 142), true, false);
        drawCup(g, 320, 478, 104, 118, new Color(245, 240, 235), new Color(177, 131, 86), true);
        drawWindowNight(g, 152, 106, 340, 210);
        drawGlow(g, 670, 510, secondary);
    }

    private static void renderScienceGenericThroat(Graphics2D g, Color primary, Color secondary, Random random) {
        drawSceneCard(g, 140, 130, 1320, 640, primary, secondary);
        drawThroatIcon(g, 492, 450, 1.34, primary, secondary);
        drawCup(g, 1042, 412, 132, 150, new Color(246, 240, 232), new Color(197, 156, 90), true);
        drawLeafSprigs(g, 300, 250, new Color(118, 166, 120), random);
        drawLeafSprigs(g, 1230, 250, new Color(118, 166, 120), random);
    }

    private static void renderScienceGenericEnt(Graphics2D g, Color primary, Color secondary, Random random) {
        drawClinicCard(g, primary, secondary);
        drawDoctorFigure(g, 444, 620, 1.0, primary, false);
        drawMonitor(g, 952, 210, 340, 230, new Color(50, 66, 92));
        drawDocument(g, 1110, 470, 176, 224, secondary);
        drawScope(g, 832, 514, primary);
    }

    private static void renderScienceDailyCare(Graphics2D g, Color primary, Color secondary, Random random) {
        drawOfficeCard(g, primary, secondary);
        drawDesk(g, 214, 616, 1180, 144, mix(secondary, new Color(146, 118, 88), 0.34));
        drawCup(g, 346, 450, 110, 124, new Color(246, 240, 232), new Color(194, 156, 90), true);
        drawHumidifier(g, 552, 436, primary);
        drawNotebook(g, 982, 452, secondary);
        drawThroatIcon(g, 1186, 504, 0.74, primary, secondary);
    }

    private static void renderScienceMedicalQa(Graphics2D g, Color primary, Color secondary, Random random) {
        drawOfficeCard(g, primary, secondary);
        drawDesk(g, 214, 616, 1180, 144, mix(secondary, new Color(146, 118, 88), 0.34));
        drawSimpleCharacter(g, 668, 598, 1.16, true, primary, false, false);
        drawNotebook(g, 982, 452, secondary);
        drawQuestionClouds(g, 946, 246, primary, secondary);
        drawCup(g, 346, 450, 110, 124, new Color(246, 240, 232), new Color(194, 156, 90), true);
    }

    private static void renderScienceVoiceHydration(Graphics2D g, Color primary, Color secondary, Random random) {
        drawOfficeCard(g, primary, secondary);
        drawDesk(g, 214, 616, 1180, 144, mix(secondary, new Color(146, 118, 88), 0.34));
        drawSimpleCharacter(g, 628, 598, 1.10, false, primary, false, true);
        drawCup(g, 346, 450, 120, 138, new Color(246, 240, 232), new Color(194, 156, 90), true);
        drawNotebook(g, 1034, 452, secondary);
        drawSoundRings(g, 760, 332, primary);
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

    private static void drawShadow(Graphics2D g, Shape shape, double dx, double dy, int alpha) {
        AffineTransform transform = AffineTransform.getTranslateInstance(dx, dy);
        Shape shifted = transform.createTransformedShape(shape);
        g.setColor(new Color(12, 16, 18, clamp(alpha)));
        g.fill(shifted);
    }

    private static void addFilmGrain(Graphics2D g, int width, int height, Random random, int whiteCount, int darkCount) {
        for (int i = 0; i < whiteCount; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int alpha = 4 + random.nextInt(8);
            int size = 1 + random.nextInt(2);
            g.setColor(new Color(255, 255, 255, alpha));
            g.fillRect(x, y, size, size);
        }
        for (int i = 0; i < darkCount; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int alpha = 3 + random.nextInt(6);
            int size = 1 + random.nextInt(2);
            g.setColor(new Color(0, 0, 0, alpha));
            g.fillRect(x, y, size, size);
        }
    }

    private static void addVignette(Graphics2D g, int width, int height, float stop, int alpha) {
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(width / 2.0, height / 2.0),
                Math.max(width, height) * 0.72f,
                new float[] {stop, 1f},
                new Color[] {new Color(0, 0, 0, 0), new Color(8, 10, 12, clamp(alpha))}));
        g.fill(new Rectangle2D.Double(0, 0, width, height));
    }
}
