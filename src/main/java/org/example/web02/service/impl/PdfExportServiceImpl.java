package org.example.web02.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.example.web02.entity.AiPlan;
import org.example.web02.entity.User;
import org.example.web02.entity.UserHealth;
import org.example.web02.mapper.UserHealthMapper;
import org.example.web02.mapper.UserMapper;
import org.example.web02.service.PdfExportService;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PdfExportServiceImpl implements PdfExportService {

    private final UserMapper userMapper;
    private final UserHealthMapper userHealthMapper;
    private final ObjectMapper objectMapper;

    public PdfExportServiceImpl(UserMapper userMapper, UserHealthMapper userHealthMapper, ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.userHealthMapper = userHealthMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] exportHealthPlanToPdf(Long userId, AiPlan plan) {
        User user = userMapper.findById(userId);
        UserHealth health = userHealthMapper.findByUserId(userId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 12, Font.NORMAL);
            Font smallFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

            addTitlePage(document, titleFont, headerFont, normalFont, user, health, plan);

            addWeeklyPlan(document, headerFont, normalFont, smallFont, plan);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("生成PDF失败", e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage());
        }
    }

    private void addTitlePage(Document document, Font titleFont, Font headerFont, Font normalFont,
                              User user, UserHealth health, AiPlan plan) throws DocumentException {
        Paragraph title = new Paragraph("Smart Health Assistant - Personalized Health Plan", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(30);
        document.add(title);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10);
        infoTable.setSpacingAfter(20);

        addInfoRow(infoTable, "Username", user != null ? user.getUsername() : "Unknown", normalFont);
        addInfoRow(infoTable, "Export Date", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()), normalFont);
        addInfoRow(infoTable, "Health Goal", health != null && health.getHealthTarget() != null ? 
                translateHealthTarget(health.getHealthTarget()) : "Not set", normalFont);
        addInfoRow(infoTable, "Daily Calorie Target", plan.getTotalCalorie() + " kcal", normalFont);
        addInfoRow(infoTable, "Version", "V" + plan.getVersionNo(), normalFont);
        addInfoRow(infoTable, "Generated", plan.getGenerateTime() != null ?
                new SimpleDateFormat("yyyy-MM-dd").format(plan.getGenerateTime()) : "Unknown", normalFont);

        document.add(infoTable);

        Paragraph summaryTitle = new Paragraph("Plan Summary", headerFont);
        summaryTitle.setSpacingBefore(20);
        summaryTitle.setSpacingAfter(15);
        document.add(summaryTitle);

        try {
            Map<String, Object> planContent = objectMapper.readValue(plan.getPlanContent(),
                    new TypeReference<Map<String, Object>>() {});

            if (planContent.containsKey("summary")) {
                Map<String, List<String>> summary = objectMapper.convertValue(
                        planContent.get("summary"),
                        new TypeReference<Map<String, List<String>>>() {});

                PdfPTable summaryTable = new PdfPTable(3);
                summaryTable.setWidthPercentage(100);

                addSummarySection(summaryTable, "Diet Tips", summary.get("diet"), normalFont);
                addSummarySection(summaryTable, "Exercise Tips", summary.get("exercise"), normalFont);
                addSummarySection(summaryTable, "Health Tips", summary.get("tips"), normalFont);

                document.add(summaryTable);
            }
        } catch (Exception e) {
            log.warn("解析计划概要失败", e);
        }

        document.newPage();
    }

    private String translateHealthTarget(String target) {
        if (target == null) return "Not set";
        return switch (target) {
            case "减肥" -> "Weight Loss";
            case "增肌" -> "Muscle Gain";
            case "维持健康" -> "Maintain Health";
            default -> target;
        };
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Paragraph(label + ":", font));
        labelCell.setBackgroundColor(new Color(245, 245, 245));
        labelCell.setPadding(8);
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Paragraph(value, font));
        valueCell.setPadding(8);
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }

    private void addSummarySection(PdfPTable table, String title, List<String> items, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(10);
        cell.setBorder(Rectangle.NO_BORDER);

        Paragraph sectionTitle = new Paragraph(title, new Font(font.getFamily(), font.getSize(), Font.BOLD));
        sectionTitle.setSpacingAfter(10);
        cell.addElement(sectionTitle);

        if (items != null && !items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                String item = cleanText(items.get(i));
                Paragraph paragraph = new Paragraph((i + 1) + ". " + item, font);
                paragraph.setSpacingBefore(5);
                cell.addElement(paragraph);
            }
        } else {
            cell.addElement(new Paragraph("No content", font));
        }

        table.addCell(cell);
    }

    private String cleanText(String text) {
        if (text == null) return "";
        return text.replaceAll("[\\u0080-\\u00FF]", "");
    }

    private void addWeeklyPlan(Document document, Font headerFont, Font normalFont, Font smallFont,
                               AiPlan plan) throws DocumentException {
        try {
            Map<String, Object> planContent = objectMapper.readValue(plan.getPlanContent(),
                    new TypeReference<Map<String, Object>>() {});

            if (planContent.containsKey("weeklyPlan")) {
                List<Map<String, Object>> weeklyPlan = objectMapper.convertValue(
                        planContent.get("weeklyPlan"),
                        new TypeReference<List<Map<String, Object>>>() {});

                for (int i = 0; i < weeklyPlan.size(); i++) {
                    Map<String, Object> dayPlan = weeklyPlan.get(i);
                    String dayName = translateDayName((String) dayPlan.get("dayName"));
                    String date = (String) dayPlan.get("date");

                    Paragraph dayTitle = new Paragraph(dayName + " (" + date + ")", headerFont);
                    dayTitle.setSpacingBefore(10);
                    dayTitle.setSpacingAfter(15);
                    document.add(dayTitle);

                    PdfPTable dayTable = new PdfPTable(2);
                    dayTable.setWidthPercentage(100);
                    dayTable.setSpacingAfter(20);

                    addMealSection(dayTable, dayPlan, normalFont, smallFont);
                    addExerciseSection(dayTable, dayPlan, normalFont, smallFont);

                    document.add(dayTable);

                    if (i < weeklyPlan.size() - 1) {
                        document.newPage();
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析周计划失败", e);
            throw new RuntimeException("解析计划内容失败: " + e.getMessage());
        }
    }

    private String translateDayName(String dayName) {
        if (dayName == null) return "Unknown";
        return switch (dayName) {
            case "周一" -> "Monday";
            case "周二" -> "Tuesday";
            case "周三" -> "Wednesday";
            case "周四" -> "Thursday";
            case "周五" -> "Friday";
            case "周六" -> "Saturday";
            case "周日" -> "Sunday";
            default -> dayName;
        };
    }

    private void addMealSection(PdfPTable dayTable, Map<String, Object> dayPlan, Font normalFont, Font smallFont) {
        PdfPCell mealCell = new PdfPCell();
        mealCell.setPadding(10);
        mealCell.setBackgroundColor(new Color(248, 249, 250));

        Paragraph mealTitle = new Paragraph("🍽️ Meal Plan", new Font(normalFont.getFamily(), normalFont.getSize(), Font.BOLD));
        mealTitle.setSpacingAfter(12);
        mealCell.addElement(mealTitle);

        List<Map<String, String>> dietList = objectMapper.convertValue(
                dayPlan.get("diet"),
                new TypeReference<List<Map<String, String>>>() {});

        if (dietList != null && !dietList.isEmpty()) {
            for (Map<String, String> meal : dietList) {
                String mealType = translateMealType(meal.get("type"));
                Paragraph mealTypePara = new Paragraph(mealType + ":", new Font(normalFont.getFamily(), normalFont.getSize(), Font.BOLD));
                mealTypePara.setSpacingBefore(8);
                mealCell.addElement(mealTypePara);

                String mealName = cleanText(meal.get("name"));
                Paragraph mealNamePara = new Paragraph("  " + mealName, normalFont);
                mealCell.addElement(mealNamePara);

                Paragraph mealCalorie = new Paragraph("  Calories: " + meal.get("calorie"), smallFont);
                mealCalorie.setSpacingAfter(4);
                mealCell.addElement(mealCalorie);
            }
        } else {
            mealCell.addElement(new Paragraph("No meal plan", normalFont));
        }

        dayTable.addCell(mealCell);
    }

    private String translateMealType(String type) {
        if (type == null) return "Unknown";
        return switch (type) {
            case "早餐" -> "Breakfast";
            case "午餐" -> "Lunch";
            case "晚餐" -> "Dinner";
            case "加餐" -> "Snack";
            default -> type;
        };
    }

    private void addExerciseSection(PdfPTable dayTable, Map<String, Object> dayPlan, Font normalFont, Font smallFont) {
        PdfPCell exerciseCell = new PdfPCell();
        exerciseCell.setPadding(10);
        exerciseCell.setBackgroundColor(new Color(240, 248, 255));

        Paragraph exerciseTitle = new Paragraph("🏃 Exercise Plan", new Font(normalFont.getFamily(), normalFont.getSize(), Font.BOLD));
        exerciseTitle.setSpacingAfter(12);
        exerciseCell.addElement(exerciseTitle);

        List<Map<String, String>> exerciseList = objectMapper.convertValue(
                dayPlan.get("exercise"),
                new TypeReference<List<Map<String, String>>>() {});

        if (exerciseList != null && !exerciseList.isEmpty()) {
            for (Map<String, String> exercise : exerciseList) {
                String exName = cleanText(exercise.get("name"));
                Paragraph exNamePara = new Paragraph("• " + exName, normalFont);
                exNamePara.setSpacingBefore(6);
                exerciseCell.addElement(exNamePara);

                Paragraph exDuration = new Paragraph("  Duration: " + exercise.get("duration"), smallFont);
                exerciseCell.addElement(exDuration);
            }
        } else {
            exerciseCell.addElement(new Paragraph("No exercise plan", normalFont));
        }

        dayTable.addCell(exerciseCell);
    }
}