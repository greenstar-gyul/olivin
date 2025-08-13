package com.olivin.app.dashboard.service;

import com.olivin.app.dashboard.mapper.BranchDashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BranchDashboardService {

    private final BranchDashboardMapper branchDashboardMapper;

    /**
     * 지점 정보 조회
     */
    public Map<String, Object> getBranchInfo(String compId) {
        try {
            Map<String, Object> branchInfo = branchDashboardMapper.getBranchInfo(compId);
            if (branchInfo == null) {
                // 기본값 설정
                Map<String, Object> defaultInfo = new HashMap<>();
                defaultInfo.put("COMP_ID", compId);
                defaultInfo.put("COMP_NAME", "알 수 없는 지점");
                defaultInfo.put("COMP_TYPE_NAME", "지점");
                return defaultInfo;
            }
            return branchInfo;
        } catch (Exception e) {
            // 에러 발생 시 기본값 반환
            Map<String, Object> defaultInfo = new HashMap<>();
            defaultInfo.put("COMP_ID", compId);
            defaultInfo.put("COMP_NAME", "지점");
            defaultInfo.put("COMP_TYPE_NAME", "");
            return defaultInfo;
        }
    }

    /**
     * 지점 KPI 데이터 조회
     */
    public Map<String, Object> getBranchKpiData(String compId) {
        Map<String, Object> result = new HashMap<>();
        
        // 당일 매출
        BigDecimal todaySales = branchDashboardMapper.getTodaySales(compId);
        BigDecimal yesterdaySales = branchDashboardMapper.getYesterdaySales(compId);
        
        result.put("todaySales", formatCurrency(todaySales));
        
        // 전일 대비 증감률
        if (yesterdaySales != null && yesterdaySales.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal dailyGrowth = todaySales.subtract(yesterdaySales)
                                               .divide(yesterdaySales, 4, RoundingMode.HALF_UP)
                                               .multiply(new BigDecimal("100"));
            result.put("dailyGrowth", formatPercentage(dailyGrowth));
        } else {
            result.put("dailyGrowth", "0.0%");
        }
        
        // 월간 매출 및 목표 달성률
        BigDecimal monthlySales = branchDashboardMapper.getMonthlySales(compId);
        BigDecimal monthlyTarget = branchDashboardMapper.getMonthlyTarget(compId);
        
        result.put("monthlySales", formatCurrency(monthlySales));
        
        if (monthlyTarget != null && monthlyTarget.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal achievement = monthlySales.divide(monthlyTarget, 4, RoundingMode.HALF_UP)
                                                .multiply(new BigDecimal("100"));
            result.put("monthlyAchievement", achievement.setScale(1, RoundingMode.HALF_UP) + "%");
        } else {
            result.put("monthlyAchievement", "목표 미설정");
        }
        
        // 현재 재고 총액
        BigDecimal inventoryValue = branchDashboardMapper.getCurrentInventoryValue(compId);
        result.put("inventoryValue", formatCurrency(inventoryValue));
        
        // 재고 품목 수
        Integer totalItems = branchDashboardMapper.getTotalInventoryItems(compId);
        Integer lowStockItems = branchDashboardMapper.getLowStockItemsCount(compId);
        Integer stockoutItems = branchDashboardMapper.getStockoutItemsCount(compId);
        
        result.put("totalInventoryItems", totalItems != null ? totalItems : 0);
        result.put("lowStockItems", lowStockItems != null ? lowStockItems : 0);
        result.put("stockoutItems", stockoutItems != null ? stockoutItems : 0);
        
        // 거래 건수 및 객단가
        Integer transactionCount = branchDashboardMapper.getTodayTransactionCount(compId);
        BigDecimal avgOrderValue = branchDashboardMapper.getTodayAverageOrderValue(compId);
        
        result.put("todayTransactions", transactionCount != null ? transactionCount : 0);
        result.put("averageOrderValue", formatCurrency(avgOrderValue));
        
        return result;
    }

    /**
     * 지점별 매출 트렌드 조회 (최근 7일)
     */
    public List<Map<String, Object>> getWeeklySalesTrend(String compId) {
        return branchDashboardMapper.getWeeklySalesTrend(compId);
    }

    /**
     * 지점별 카테고리별 매출 구성
     */
    public List<Map<String, Object>> getCategorySales(String compId) {
        List<Map<String, Object>> categorySales = branchDashboardMapper.getCategorySalesDistribution(compId);
        
        // 전체 매출 대비 비율 계산
        BigDecimal totalSales = categorySales.stream()
            .map(item -> (BigDecimal) item.get("SALES"))
            .filter(sales -> sales != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        categorySales.forEach(category -> {
            BigDecimal sales = (BigDecimal) category.get("SALES");
            if (sales != null && totalSales.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = sales.divide(totalSales, 4, RoundingMode.HALF_UP)
                                            .multiply(new BigDecimal("100"));
                category.put("percentage", percentage.setScale(1, RoundingMode.HALF_UP));
            } else {
                category.put("percentage", BigDecimal.ZERO);
            }
        });
        
        return categorySales;
    }

    /**
     * 발주 필요 상품 리스트
     */
    public List<Map<String, Object>> getLowStockItems(String compId) {
        List<Map<String, Object>> items = branchDashboardMapper.getLowStockItems(compId);
        
        // 추가 정보 처리
        items.forEach(item -> {
            BigDecimal currentStock = (BigDecimal) item.get("CURRENT_STOCK");
            BigDecimal safetyStock = (BigDecimal) item.get("SAFETY_STOCK");
            
            if (currentStock != null && safetyStock != null && safetyStock.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal stockRatio = currentStock.divide(safetyStock, 2, RoundingMode.HALF_UP)
                                                  .multiply(new BigDecimal("100"));
                item.put("stockRatio", stockRatio.setScale(1, RoundingMode.HALF_UP) + "%");
                
                // 긴급도 설정
                if (stockRatio.compareTo(new BigDecimal("20")) <= 0) {
                    item.put("urgency", "HIGH");
                } else if (stockRatio.compareTo(new BigDecimal("50")) <= 0) {
                    item.put("urgency", "MEDIUM");
                } else {
                    item.put("urgency", "LOW");
                }
            }
        });
        
        return items;
    }

    /**
     * 품절 임박 상품 리스트
     */
    public List<Map<String, Object>> getCriticalStockItems(String compId) {
        return branchDashboardMapper.getCriticalStockItems(compId);
    }

    /**
     * 상위 판매 상품 TOP 5
     */
    public List<Map<String, Object>> getTopSellingProducts(String compId) {
        return branchDashboardMapper.getTopSellingProducts(compId);
    }

    /**
     * 재고 회전율 계산
     */
    public Map<String, Object> getInventoryTurnover(String compId) {
        Map<String, Object> result = new HashMap<>();
        
        BigDecimal cogs = branchDashboardMapper.getCostOfGoodsSold(compId);
        BigDecimal avgInventory = branchDashboardMapper.getAverageInventoryValue(compId);
        
        if (avgInventory != null && avgInventory.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal turnover = cogs.divide(avgInventory, 2, RoundingMode.HALF_UP);
            result.put("turnoverRate", turnover.setScale(1, RoundingMode.HALF_UP) + "회");
            
            // 회전 주기 (일)
            if (turnover.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal turnoverDays = new BigDecimal("365").divide(turnover, 0, RoundingMode.HALF_UP);
                result.put("turnoverDays", turnoverDays + "일");
            }
        } else {
            result.put("turnoverRate", "0.0회");
            result.put("turnoverDays", "계산불가");
        }
        
        return result;
    }

    /**
     * 지점 알림 목록
     */
    public List<Map<String, Object>> getBranchAlerts(String compId) {
        List<Map<String, Object>> alerts = branchDashboardMapper.getBranchAlerts(compId);
        
        // 알림 우선순위 및 색상 설정
        alerts.forEach(alert -> {
            String alertType = (String) alert.get("ALERT_TYPE");
            
            switch (alertType != null ? alertType : "INFO") {
                case "STOCKOUT":
                case "CRITICAL_STOCK":
                    alert.put("priority", "HIGH");
                    alert.put("color", "#FF4444");
                    alert.put("icon", "📦");
                    break;
                case "LOW_STOCK":
                    alert.put("priority", "MEDIUM");
                    alert.put("color", "#FF8800");
                    alert.put("icon", "⚠️");
                    break;
                case "ORDER_REQUIRED":
                    alert.put("priority", "MEDIUM");
                    alert.put("color", "#FFA500");
                    alert.put("icon", "🛒");
                    break;
                default:
                    alert.put("priority", "LOW");
                    alert.put("color", "#888888");
                    alert.put("icon", "ℹ️");
            }
        });
        
        return alerts;
    }

    // 🔥 본사용 추가 메서드들
    /**
     * 모든 지점 목록 조회 (본사 전용)
     */
    public List<Map<String, Object>> getAllBranches() {
        try {
            return branchDashboardMapper.getAllBranches();
        } catch (Exception e) {
            // 에러 시 빈 리스트 반환
            return new ArrayList<>();
        }
    }

    /**
     * 모든 지점 통합 KPI (본사 전용)
     */
    public Map<String, Object> getAllBranchesKpi() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 전체 지점 수
            Integer totalBranches = branchDashboardMapper.getTotalBranchCount();
            result.put("totalBranches", totalBranches != null ? totalBranches : 0);
            
            // 전체 매출 합계
            BigDecimal totalSales = branchDashboardMapper.getAllBranchesTotalSales();
            result.put("totalSales", formatCurrency(totalSales));
            
            // 각 지점별 요약 정보
            List<Map<String, Object>> branchSummaries = branchDashboardMapper.getAllBranchesSummary();
            result.put("branchSummaries", branchSummaries);
            
            return result;
        } catch (Exception e) {
            // 에러 시 기본값 반환
            result.put("totalBranches", 0);
            result.put("totalSales", "0원");
            result.put("branchSummaries", new ArrayList<>());
            return result;
        }
    }

    // 유틸리티 메서드들
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0원";
        
        // 천 단위 구분 콤마를 사용한 원 단위 표시
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return df.format(amount) + "원";
    }

    private String formatPercentage(BigDecimal percentage) {
        if (percentage == null) return "0.0%";
        String sign = percentage.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return sign + percentage.setScale(1, RoundingMode.HALF_UP) + "%";
    }
}