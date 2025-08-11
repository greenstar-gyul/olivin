package com.olivin.app.dashboard.service;

import com.olivin.app.dashboard.mapper.SupplierDashboardMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierDashboardService {

    private final SupplierDashboardMapper supplierDashboardMapper;

    /**
     * 공급업체 ID 유효성 검사
     */
    private String validateSupplierId(String supplierId) {
        if (supplierId == null || supplierId.trim().isEmpty()) {
            log.warn("공급업체 ID가 제공되지 않았습니다.");
            return null;
        }
        return supplierId.trim();
    }

    /**
     * 공급업체 목록 조회 (시스템 관리자용)
     */
    public List<Map<String, Object>> getAvailableSuppliers() {
        log.info("공급업체 목록 조회 시작");

        try {
            List<Map<String, Object>> suppliers = supplierDashboardMapper.getAvailableSuppliers();
            log.info("공급업체 목록 {} 개 조회 완료", suppliers.size());
            return suppliers;
        } catch (Exception e) {
            log.error("공급업체 목록 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 공급업체 정보 조회
     */
    public Map<String, Object> getSupplierInfo(String supplierId) {
        log.info("공급업체 정보 조회 시작 - supplierId: {}", supplierId);

        try {
            String targetSupplierId = validateSupplierId(supplierId);
            if (targetSupplierId == null) {
                log.warn("공급업체 ID를 확인할 수 없습니다.");
                return new HashMap<>();
            }

            Map<String, Object> supplierInfo = supplierDashboardMapper.getSupplierInfo(targetSupplierId);
            log.info("공급업체 정보 조회 완료 - supplierId: {}, result: {}", targetSupplierId, supplierInfo);
            return supplierInfo != null ? supplierInfo : new HashMap<>();
        } catch (Exception e) {
            log.error("공급업체 정보 조회 중 오류 발생 - supplierId: {}", supplierId, e);
            return new HashMap<>();
        }
    }

    /**
     * 공급업체 KPI 데이터 조회
     */
    public Map<String, Object> getKpiData(String supplierId) {
        log.info("공급업체 KPI 데이터 조회 시작 - supplierId: {}", supplierId);

        try {
            String targetSupplierId = validateSupplierId(supplierId);
            if (targetSupplierId == null) {
                log.warn("공급업체 ID를 확인할 수 없습니다.");
                return createEmptyKpiData();
            }

            Map<String, Object> result = new HashMap<>();
            DecimalFormat df = new DecimalFormat("#,###");
            DecimalFormat percentFormat = new DecimalFormat("0.0");

            // 1. 월간 발주량
            Integer monthlyOrders = supplierDashboardMapper.getMonthlyOrderCount(targetSupplierId);
            Integer lastMonthOrders = supplierDashboardMapper.getLastMonthOrderCount(targetSupplierId);
            log.info("발주량 데이터 - supplierId: {}, current: {}, previous: {}", targetSupplierId, monthlyOrders, lastMonthOrders);
            
            result.put("monthlyOrders", monthlyOrders + "개");

            // 발주량 증감 계산
            Integer ordersChange = monthlyOrders - (lastMonthOrders != null ? lastMonthOrders : 0);
            String growthStr = (ordersChange >= 0 ? "+" : "") + ordersChange + "개";
            result.put("ordersGrowth", growthStr);

            // 2. 발주 완료율
            Map<String, Object> completionData = supplierDashboardMapper.getOrderCompletionRate(targetSupplierId);
            log.info("완료율 데이터 - supplierId: {}, result: {}", targetSupplierId, completionData);
            if (completionData != null) {
                Integer totalOrders = completionData.get("TOTAL_ORDERS") != null ? 
                    ((Number) completionData.get("TOTAL_ORDERS")).intValue() : 0;
                Integer completedOrders = completionData.get("COMPLETED_ORDERS") != null ? 
                    ((Number) completionData.get("COMPLETED_ORDERS")).intValue() : 0;

                log.info("완료율 세부 데이터 - total: {}, completed: {}", totalOrders, completedOrders);

                if (totalOrders != null && totalOrders > 0) {
                    double completionRate = ((double) completedOrders / totalOrders) * 100;
                    result.put("completionRate", percentFormat.format(completionRate) + "%");

                    // 전월 대비 처리율 변화
                    Map<String, Object> lastMonthCompletion = supplierDashboardMapper.getLastMonthCompletionRate(targetSupplierId);
                    if (lastMonthCompletion != null) {
                        Integer lastMonthTotal = lastMonthCompletion.get("TOTAL_ORDERS") != null ?
                            ((Number) lastMonthCompletion.get("TOTAL_ORDERS")).intValue() : 0;
                        Integer lastMonthCompleted = lastMonthCompletion.get("COMPLETED_ORDERS") != null ?
                            ((Number) lastMonthCompletion.get("COMPLETED_ORDERS")).intValue() : 0;

                        if (lastMonthTotal != null && lastMonthTotal > 0) {
                            double lastCompletionRate = ((double) lastMonthCompleted / lastMonthTotal) * 100;
                            double completionChange = completionRate - lastCompletionRate;
                            String changeStr = (completionChange >= 0 ? "+" : "") + percentFormat.format(completionChange) + "%";
                            result.put("completionRateChange", changeStr);
                        } else {
                            result.put("completionRateChange", "+0.0%");
                        }
                    } else {
                        result.put("completionRateChange", "+0.0%");
                    }
                } else {
                    result.put("completionRate", "0%");
                    result.put("completionRateChange", "+0.0%");
                }
            } else {
                result.put("completionRate", "0%");
                result.put("completionRateChange", "+0.0%");
            }

            // 3. 평균 납기일
            Double avgDeliveryTime = supplierDashboardMapper.getAverageDeliveryTime(targetSupplierId);
            Double lastMonthAvgDeliveryTime = supplierDashboardMapper.getLastMonthAverageDeliveryTime(targetSupplierId);

            result.put("avgDeliveryTime", Math.round(avgDeliveryTime != null ? avgDeliveryTime : 0) + "일");

            if (lastMonthAvgDeliveryTime != null) {
                double deliveryChange = (avgDeliveryTime != null ? avgDeliveryTime : 0) - lastMonthAvgDeliveryTime;
                String changeStr = (deliveryChange >= 0 ? "+" : "") + Math.round(deliveryChange) + "일";
                result.put("deliveryTimeChange", changeStr);
            } else {
                result.put("deliveryTimeChange", "0일");
            }

            // 4. 월간 발주 건수
            Double qualityScore = supplierDashboardMapper.getQualityScore(targetSupplierId);
            Double lastMonthQualityScore = supplierDashboardMapper.getLastMonthQualityScore(targetSupplierId);

            result.put("qualityScore", Math.round(qualityScore != null ? qualityScore : 0) + "건");

            if (lastMonthQualityScore != null) {
                double qualityChange = (qualityScore != null ? qualityScore : 0) - lastMonthQualityScore;
                String changeStr = (qualityChange >= 0 ? "+" : "") + Math.round(qualityChange) + "건";
                result.put("qualityScoreChange", changeStr);
            } else {
                result.put("qualityScoreChange", "+0건");
            }

            log.info("공급업체 KPI 데이터 조회 완료: {}", result);
            return result;

        } catch (Exception e) {
            log.error("공급업체 KPI 데이터 조회 중 오류 발생", e);
            return createEmptyKpiData();
        }
    }

    /**
     * 진행 중인 발주 현황 조회
     */
    public List<Map<String, Object>> getActiveOrders(String supplierId) {
        log.info("진행 중인 발주 현황 조회 시작 - supplierId: {}", supplierId);

        try {
            String targetSupplierId = validateSupplierId(supplierId);
            if (targetSupplierId == null) {
                log.warn("공급업체 ID를 확인할 수 없습니다.");
                return new ArrayList<>();
            }

            List<Map<String, Object>> orders = supplierDashboardMapper.getActiveOrders(targetSupplierId);
            log.info("진행 중인 발주 {} 건 조회 완료", orders.size());
            return orders;
        } catch (Exception e) {
            log.error("진행 중인 발주 현황 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 상위 공급 상품 조회
     */
    public List<Map<String, Object>> getTopProducts(String supplierId) {
        log.info("상위 공급 상품 조회 시작 - supplierId: {}", supplierId);

        try {
            String targetSupplierId = validateSupplierId(supplierId);
            if (targetSupplierId == null) {
                log.warn("공급업체 ID를 확인할 수 없습니다.");
                return new ArrayList<>();
            }

            List<Map<String, Object>> products = supplierDashboardMapper.getTopProducts(targetSupplierId);
            log.info("상위 공급 상품 {} 개 조회 완료", products.size());
            return products;
        } catch (Exception e) {
            log.error("상위 공급 상품 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 월간 수주 트렌드 조회
     */
    public List<Map<String, Object>> getOrderTrend(String supplierId) {
        log.info("월간 수주 트렌드 조회 시작 - supplierId: {}", supplierId);

        try {
            String targetSupplierId = validateSupplierId(supplierId);
            if (targetSupplierId == null) {
                log.warn("공급업체 ID를 확인할 수 없습니다.");
                return new ArrayList<>();
            }

            List<Map<String, Object>> trendData = supplierDashboardMapper.getOrderTrend(targetSupplierId);
            log.info("수주 트렌드 데이터 {} 건 조회 완료", trendData.size());
            return trendData;
        } catch (Exception e) {
            log.error("월간 수주 트렌드 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 카테고리별 공급 비중 조회
     */
    public List<Map<String, Object>> getCategorySupply(String supplierId) {
        log.info("카테고리별 공급 비중 조회 시작 - supplierId: {}", supplierId);

        try {
            String targetSupplierId = validateSupplierId(supplierId);
            if (targetSupplierId == null) {
                log.warn("공급업체 ID를 확인할 수 없습니다.");
                return new ArrayList<>();
            }

            List<Map<String, Object>> categoryData = supplierDashboardMapper.getCategorySupply(targetSupplierId);
            log.info("카테고리별 공급 데이터 {} 건 조회 완료", categoryData.size());
            return categoryData;
        } catch (Exception e) {
            log.error("카테고리별 공급 비중 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 품질/배송 관련 알림 조회
     */
    public List<Map<String, Object>> getAlerts(String supplierId) {
        log.info("공급업체 알림 조회 시작 - supplierId: {}", supplierId);

        try {
            String targetSupplierId = validateSupplierId(supplierId);
            if (targetSupplierId == null) {
                log.warn("공급업체 ID를 확인할 수 없습니다.");
                return new ArrayList<>();
            }

            List<Map<String, Object>> alerts = supplierDashboardMapper.getAlerts(targetSupplierId);
            log.info("공급업체 알림 {} 건 조회 완료", alerts.size());
            return alerts;
        } catch (Exception e) {
            log.error("공급업체 알림 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 모든 주문 상태 조회 (디버깅용)
     */
    public List<Map<String, Object>> getAllOrderStatus(String supplierId) {
        log.info("모든 주문 상태 조회 시작 - supplierId: {}", supplierId);

        try {
            String targetSupplierId = validateSupplierId(supplierId);
            if (targetSupplierId == null) {
                log.warn("공급업체 ID를 확인할 수 없습니다.");
                return new ArrayList<>();
            }

            List<Map<String, Object>> orders = supplierDashboardMapper.getAllOrderStatus(targetSupplierId);
            log.info("모든 주문 상태 {} 건 조회 완료", orders.size());
            return orders;
        } catch (Exception e) {
            log.error("모든 주문 상태 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 기본 KPI 데이터 생성
     */
    private Map<String, Object> createEmptyKpiData() {
        Map<String, Object> emptyData = new HashMap<>();
        emptyData.put("monthlyOrders", "0개");
        emptyData.put("ordersGrowth", "+0개");
        emptyData.put("completionRate", "0%");
        emptyData.put("completionRateChange", "+0.0%");
        emptyData.put("avgDeliveryTime", "0일");
        emptyData.put("deliveryTimeChange", "0일");
        emptyData.put("qualityScore", "0건");
        emptyData.put("qualityScoreChange", "+0건");
        return emptyData;
    }
}