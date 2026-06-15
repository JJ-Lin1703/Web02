package org.example.web02.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
/**
 * PDF导出服务实现类
 * 负责将AI健康计划导出为PDF文档，支持中文显示和精美排版
 */
public class PdfExportServiceImpl implements PdfExportService {

    /** 用户数据访问层 */
    private final UserMapper userMapper;
    /** 用户健康档案数据访问层 */
    private final UserHealthMapper userHealthMapper;
    /** JSON序列化工具 */
    private final ObjectMapper objectMapper;

    /** 中文字体基础对象 */
    private BaseFont chineseBaseFont;

    /**
     * 构造函数注入依赖
     * @param userMapper 用户Mapper
     * @param userHealthMapper 用户健康档案Mapper
     * @param objectMapper JSON序列化工具
     */
    public PdfExportServiceImpl(UserMapper userMapper, UserHealthMapper userHealthMapper, ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.userHealthMapper = userHealthMapper;
        this.objectMapper = objectMapper;
        initChineseFont();
    }

    /**
     * 初始化中文字体支持
     * 依次尝试从系统字体路径加载，失败则回退到classpath字体
     * 支持的字体路径：Windows黑体、宋体、微软雅黑；macOS苹方；Linux文泉驿微黑
     */
    private void initChineseFont() {
        try {
            // 尝试从系统字体加载中文字体
            // Windows系统常用中文字体路径
            String[] fontPaths = {
                "C:/Windows/Fonts/simhei.ttf",      // 黑体
                "C:/Windows/Fonts/simsun.ttc",      // 宋体
                "C:/Windows/Fonts/msyh.ttc",        // 微软雅黑
                "/System/Library/Fonts/PingFang.ttc", // macOS
                "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc" // Linux
            };

            for (String path : fontPaths) {
                try {
                    chineseBaseFont = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    log.info("成功加载中文字体: {}", path);
                    return;
                } catch (Exception e) {
                    log.debug("字体路径 {} 不可用: {}", path, e.getMessage());
                }
            }

            // 如果系统字体都不可用，尝试从classpath加载
            try {
                ClassPathResource fontResource = new ClassPathResource("fonts/simhei.ttf");
                if (fontResource.exists()) {
                    InputStream is = fontResource.getInputStream();
                    chineseBaseFont = BaseFont.createFont("fonts/simhei.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    log.info("从classpath加载中文字体成功");
                    return;
                }
            } catch (Exception e) {
                log.debug("classpath字体不可用: {}", e.getMessage());
            }

            log.warn("未能加载中文字体，PDF中文内容可能无法正常显示");
        } catch (Exception e) {
            log.error("初始化中文字体失败", e);
        }
    }

    /**
     * 获取中文字体对象
     * 如果中文字体未加载成功，则返回默认Helvetica字体
     * 
     * @param size 字体大小
     * @param style 字体样式（Font.BOLD, Font.NORMAL等）
     * @return Font对象
     */
    private Font getChineseFont(float size, int style) {
        if (chineseBaseFont != null) {
            return new Font(chineseBaseFont, size, style);
        }
        return new Font(Font.HELVETICA, size, style);
    }

    /**
     * 导出健康计划为PDF
     * 生成包含用户信息、计划概要和一周详细计划的PDF文档
     * 
     * @param userId 用户ID
     * @param plan AI健康计划实体
     * @return PDF文件的字节数组
     * @throws RuntimeException 生成PDF失败时抛出
     */
    @Override
    public byte[] exportHealthPlanToPdf(Long userId, AiPlan plan) {
        User user = userMapper.findById(userId);
        UserHealth health = userHealthMapper.findByUserId(userId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            // 使用中文字体
            Font titleFont = getChineseFont(20, Font.BOLD);
            Font headerFont = getChineseFont(16, Font.BOLD);
            Font subHeaderFont = getChineseFont(14, Font.BOLD);
            Font normalFont = getChineseFont(12, Font.NORMAL);
            Font smallFont = getChineseFont(10, Font.NORMAL);
            Font boldFont = getChineseFont(12, Font.BOLD);

            addTitlePage(document, titleFont, headerFont, subHeaderFont, normalFont, smallFont, boldFont, user, health, plan);

            addWeeklyPlan(document, headerFont, subHeaderFont, normalFont, smallFont, boldFont, plan);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("生成PDF失败", e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage());
        }
    }

    /**
     * 添加标题页内容
     * 包含标题、用户信息表格和计划概要
     * 
     * @param document PDF文档对象
     * @param titleFont 标题字体
     * @param headerFont 头部字体
     * @param subHeaderFont 子头部字体
     * @param normalFont 普通字体
     * @param smallFont 小号字体
     * @param boldFont 粗体字体
     * @param user 用户实体
     * @param health 用户健康档案
     * @param plan AI健康计划
     * @throws DocumentException PDF文档操作异常
     */
    private void addTitlePage(Document document, Font titleFont, Font headerFont, Font subHeaderFont,
                              Font normalFont, Font smallFont, Font boldFont,
                              User user, UserHealth health, AiPlan plan) throws DocumentException {

        // 标题
        Paragraph title = new Paragraph("智能健康助手 - 个性化健康计划", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(20);
        title.setSpacingAfter(30);

        // 标题背景色块
        PdfPTable titleTable = new PdfPTable(1);
        titleTable.setWidthPercentage(100);
        PdfPCell titleCell = new PdfPCell(title);
        titleCell.setBackgroundColor(new Color(52, 152, 219)); // 蓝色背景
        titleCell.setPadding(15);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleTable.addCell(titleCell);
        document.add(titleTable);

        // 空行
        addEmptyLine(document, 1);

        // 用户信息表格
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10);
        infoTable.setSpacingAfter(15);
        infoTable.setWidths(new float[]{1f, 2f});

        addInfoRow(infoTable, "用户名", user != null ? user.getUsername() : "未知", boldFont, normalFont, new Color(236, 240, 241));
        addInfoRow(infoTable, "导出日期", new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date()), boldFont, normalFont, new Color(236, 240, 241));
        addInfoRow(infoTable, "健康目标", health != null && health.getHealthTarget() != null ? health.getHealthTarget() : "未设置", boldFont, normalFont, new Color(236, 240, 241));
        addInfoRow(infoTable, "每日热量目标", plan.getTotalCalorie() + " 千卡", boldFont, normalFont, new Color(236, 240, 241));
        addInfoRow(infoTable, "计划版本", "第 " + plan.getVersionNo() + " 版", boldFont, normalFont, new Color(236, 240, 241));
        addInfoRow(infoTable, "生成时间", plan.getGenerateTime() != null ?
                new SimpleDateFormat("yyyy年MM月dd日").format(plan.getGenerateTime()) : "未知", boldFont, normalFont, new Color(236, 240, 241));

        document.add(infoTable);

        addEmptyLine(document, 1);

        // 计划概要标题
        Paragraph summaryTitle = new Paragraph("📋 计划概要", headerFont);
        summaryTitle.setSpacingBefore(10);
        summaryTitle.setSpacingAfter(15);
        document.add(summaryTitle);

        // 解析并显示概要内容
        try {
            Map<String, Object> planContent = objectMapper.readValue(plan.getPlanContent(),
                    new TypeReference<Map<String, Object>>() {});

            if (planContent.containsKey("summary")) {
                Map<String, List<String>> summary = objectMapper.convertValue(
                        planContent.get("summary"),
                        new TypeReference<Map<String, List<String>>>() {});

                // 饮食建议
                addSummaryBlock(document, "🥗 饮食建议", summary.get("diet"), subHeaderFont, normalFont, new Color(46, 204, 113));

                // 运动建议
                addSummaryBlock(document, "💪 运动建议", summary.get("exercise"), subHeaderFont, normalFont, new Color(155, 89, 182));

                // 健康提示
                addSummaryBlock(document, "💡 健康提示", summary.get("tips"), subHeaderFont, normalFont, new Color(241, 196, 15));
            }
        } catch (Exception e) {
            log.warn("解析计划概要失败: {}", e.getMessage());
            Paragraph errorMsg = new Paragraph("暂无概要信息", normalFont);
            errorMsg.setAlignment(Element.ALIGN_CENTER);
            document.add(errorMsg);
        }

        document.newPage();
    }

    /**
     * 添加空行
     * 
     * @param document PDF文档对象
     * @param number 空行数量
     * @throws DocumentException PDF文档操作异常
     */
    private void addEmptyLine(Document document, int number) throws DocumentException {
        for (int i = 0; i < number; i++) {
            document.add(new Paragraph(" ", new Font(Font.HELVETICA, 12)));
        }
    }

    /**
     * 添加信息表格行
     * 
     * @param table PDF表格对象
     * @param label 标签文本
     * @param value 值文本
     * @param labelFont 标签字体
     * @param valueFont 值字体
     * @param bgColor 背景颜色
     */
    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont, Color bgColor) {
        PdfPCell labelCell = new PdfPCell(new Paragraph(label, labelFont));
        labelCell.setBackgroundColor(bgColor);
        labelCell.setPadding(10);
        labelCell.setBorderColor(new Color(189, 195, 199));
        labelCell.setBorderWidth(0.5f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Paragraph(value, valueFont));
        valueCell.setPadding(10);
        valueCell.setBorderColor(new Color(189, 195, 199));
        valueCell.setBorderWidth(0.5f);
        table.addCell(valueCell);
    }

    /**
     * 添加概要区块（饮食建议、运动建议、健康提示）
     * 
     * @param document PDF文档对象
     * @param title 区块标题
     * @param items 项目列表
     * @param titleFont 标题字体
     * @param contentFont 内容字体
     * @param accentColor 强调色
     * @throws DocumentException PDF文档操作异常
     */
    private void addSummaryBlock(Document document, String title, List<String> items, Font titleFont, Font contentFont, Color accentColor) throws DocumentException {
        // 标题行
        PdfPTable titleTable = new PdfPTable(1);
        titleTable.setWidthPercentage(100);
        titleTable.setSpacingBefore(10);
        titleTable.setSpacingAfter(5);

        PdfPCell titleCell = new PdfPCell(new Paragraph(title, titleFont));
        titleCell.setBackgroundColor(accentColor);
        titleCell.setPadding(8);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleTable.addCell(titleCell);
        document.add(titleTable);

        // 内容列表
        PdfPTable contentTable = new PdfPTable(1);
        contentTable.setWidthPercentage(100);
        contentTable.setSpacingAfter(15);

        if (items != null && !items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                String item = items.get(i);
                if (item == null || item.trim().isEmpty()) continue;

                PdfPCell contentCell = new PdfPCell(new Paragraph("  " + (i + 1) + ". " + item, contentFont));
                contentCell.setBackgroundColor(new Color(245, 245, 245));
                contentCell.setPadding(8);
                contentCell.setBorderColor(new Color(220, 220, 220));
                contentCell.setBorderWidth(0.3f);
                contentTable.addCell(contentCell);
            }
        } else {
            PdfPCell emptyCell = new PdfPCell(new Paragraph("  暂无相关建议", contentFont));
            emptyCell.setBackgroundColor(new Color(245, 245, 245));
            emptyCell.setPadding(8);
            emptyCell.setBorderColor(new Color(220, 220, 220));
            contentTable.addCell(emptyCell);
        }

        document.add(contentTable);
    }

    /**
     * 添加一周详细计划
     * 逐天渲染饮食和运动安排，每两天换一页
     * 
     * @param document PDF文档对象
     * @param headerFont 头部字体
     * @param subHeaderFont 子头部字体
     * @param normalFont 普通字体
     * @param smallFont 小号字体
     * @param boldFont 粗体字体
     * @param plan AI健康计划
     * @throws DocumentException PDF文档操作异常
     */
    private void addWeeklyPlan(Document document, Font headerFont, Font subHeaderFont, Font normalFont, Font smallFont, Font boldFont,
                               AiPlan plan) throws DocumentException {
        try {
            Map<String, Object> planContent = objectMapper.readValue(plan.getPlanContent(),
                    new TypeReference<Map<String, Object>>() {});

            if (planContent.containsKey("weeklyPlan")) {
                List<Map<String, Object>> weeklyPlan = objectMapper.convertValue(
                        planContent.get("weeklyPlan"),
                        new TypeReference<List<Map<String, Object>>>() {});

                // 周计划标题
                Paragraph weeklyTitle = new Paragraph("📅 一周详细计划", headerFont);
                weeklyTitle.setAlignment(Element.ALIGN_CENTER);
                weeklyTitle.setSpacingBefore(10);
                weeklyTitle.setSpacingAfter(20);
                document.add(weeklyTitle);

                for (int i = 0; i < weeklyPlan.size(); i++) {
                    Map<String, Object> dayPlan = weeklyPlan.get(i);
                    String dayName = (String) dayPlan.get("dayName");
                    String date = (String) dayPlan.get("date");

                    // 日期标题
                    PdfPTable dayTitleTable = new PdfPTable(1);
                    dayTitleTable.setWidthPercentage(100);
                    dayTitleTable.setSpacingBefore(15);
                    dayTitleTable.setSpacingAfter(10);

                    PdfPCell dayTitleCell = new PdfPCell(new Paragraph(dayName + " (" + date + ")", subHeaderFont));
                    dayTitleCell.setBackgroundColor(new Color(52, 152, 219));
                    dayTitleCell.setPadding(10);
                    dayTitleCell.setBorder(Rectangle.NO_BORDER);
                    dayTitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    dayTitleTable.addCell(dayTitleCell);
                    document.add(dayTitleTable);

                    // 饮食和运动并排显示
                    PdfPTable dayTable = new PdfPTable(2);
                    dayTable.setWidthPercentage(100);
                    dayTable.setSpacingAfter(20);
                    dayTable.setWidths(new float[]{1f, 1f});

                    addMealSection(dayTable, dayPlan, subHeaderFont, normalFont, smallFont, boldFont);
                    addExerciseSection(dayTable, dayPlan, subHeaderFont, normalFont, smallFont, boldFont);

                    document.add(dayTable);

                    // 每两天换一页，避免内容过于拥挤
                    if (i % 2 == 1 && i < weeklyPlan.size() - 1) {
                        document.newPage();
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析周计划失败", e);
            throw new RuntimeException("解析计划内容失败: " + e.getMessage());
        }
    }

    /**
     * 添加餐食安排区块
     * 显示早餐、午餐、晚餐及加餐的详细内容和热量信息
     * 
     * @param dayTable 日期表格对象
     * @param dayPlan 单日计划数据
     * @param titleFont 标题字体
     * @param normalFont 普通字体
     * @param smallFont 小号字体
     * @param boldFont 粗体字体
     */
    private void addMealSection(PdfPTable dayTable, Map<String, Object> dayPlan, Font titleFont, Font normalFont, Font smallFont, Font boldFont) {
        PdfPCell mealCell = new PdfPCell();
        mealCell.setPadding(10);
        mealCell.setBackgroundColor(new Color(255, 250, 240)); // 浅米色背景
        mealCell.setBorderColor(new Color(200, 200, 200));
        mealCell.setBorderWidth(1f);

        // 餐食标题
        Paragraph mealTitle = new Paragraph("🥗 餐食安排", titleFont);
        mealTitle.setAlignment(Element.ALIGN_CENTER);
        mealTitle.setSpacingAfter(12);
        mealCell.addElement(mealTitle);

        List<Map<String, String>> dietList = objectMapper.convertValue(
                dayPlan.get("diet"),
                new TypeReference<List<Map<String, String>>>() {});

        if (dietList != null && !dietList.isEmpty()) {
            for (Map<String, String> meal : dietList) {
                String mealType = meal.get("type");
                if (mealType == null) mealType = "未知";

                // 餐食类型标题
                Paragraph mealTypePara = new Paragraph("【" + mealType + "】", boldFont);
                mealTypePara.setSpacingBefore(8);
                mealTypePara.setSpacingAfter(3);
                mealCell.addElement(mealTypePara);

                // 餐食内容
                String mealName = meal.get("name");
                if (mealName != null && !mealName.isEmpty()) {
                    Paragraph mealNamePara = new Paragraph("  " + mealName, normalFont);
                    mealCell.addElement(mealNamePara);
                }

                // 热量信息
                String calorie = meal.get("calorie");
                if (calorie != null && !calorie.isEmpty()) {
                    Paragraph mealCalorie = new Paragraph("  热量：" + calorie, smallFont);
                    mealCalorie.setSpacingAfter(5);
                    mealCell.addElement(mealCalorie);
                }
            }
        } else {
            Paragraph noMeal = new Paragraph("暂无餐食安排", normalFont);
            noMeal.setAlignment(Element.ALIGN_CENTER);
            mealCell.addElement(noMeal);
        }

        dayTable.addCell(mealCell);
    }

    /**
     * 添加运动安排区块
     * 显示运动名称和时长信息
     * 
     * @param dayTable 日期表格对象
     * @param dayPlan 单日计划数据
     * @param titleFont 标题字体
     * @param normalFont 普通字体
     * @param smallFont 小号字体
     * @param boldFont 粗体字体
     */
    private void addExerciseSection(PdfPTable dayTable, Map<String, Object> dayPlan, Font titleFont, Font normalFont, Font smallFont, Font boldFont) {
        PdfPCell exerciseCell = new PdfPCell();
        exerciseCell.setPadding(10);
        exerciseCell.setBackgroundColor(new Color(240, 255, 240)); // 浅绿色背景
        exerciseCell.setBorderColor(new Color(200, 200, 200));
        exerciseCell.setBorderWidth(1f);

        // 运动标题
        Paragraph exerciseTitle = new Paragraph("💪 运动安排", titleFont);
        exerciseTitle.setAlignment(Element.ALIGN_CENTER);
        exerciseTitle.setSpacingAfter(12);
        exerciseCell.addElement(exerciseTitle);

        List<Map<String, String>> exerciseList = objectMapper.convertValue(
                dayPlan.get("exercise"),
                new TypeReference<List<Map<String, String>>>() {});

        if (exerciseList != null && !exerciseList.isEmpty()) {
            for (int i = 0; i < exerciseList.size(); i++) {
                Map<String, String> exercise = exerciseList.get(i);

                String exName = exercise.get("name");
                if (exName == null) exName = "未知运动";

                // 运动名称
                Paragraph exNamePara = new Paragraph((i + 1) + ". " + exName, boldFont);
                exNamePara.setSpacingBefore(8);
                exerciseCell.addElement(exNamePara);

                // 运动时长
                String duration = exercise.get("duration");
                if (duration != null && !duration.isEmpty()) {
                    Paragraph exDuration = new Paragraph("   时长：" + duration, smallFont);
                    exDuration.setSpacingAfter(5);
                    exerciseCell.addElement(exDuration);
                }
            }
        } else {
            Paragraph noExercise = new Paragraph("暂无运动安排", normalFont);
            noExercise.setAlignment(Element.ALIGN_CENTER);
            exerciseCell.addElement(noExercise);
        }

        dayTable.addCell(exerciseCell);
    }
}