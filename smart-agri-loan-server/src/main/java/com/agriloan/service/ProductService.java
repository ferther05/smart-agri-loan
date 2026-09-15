package com.agriloan.service;

import com.agriloan.aop.OpLog;
import com.agriloan.common.BusinessException;
import com.agriloan.domain.Product;
import com.agriloan.domain.ProductType;
import com.agriloan.domain.User;
import com.agriloan.dto.ProductDTO;
import com.agriloan.dto.ProductSaveRequest;
import com.agriloan.repository.ProductRepository;
import com.agriloan.repository.UserRepository;
import com.agriloan.security.SecurityUtils;
import com.agriloan.support.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 贷款产品服务
 *
 * <p>查询：按当前用户信用等级附带"能否申请"的自适应判定；
 * 维护：审批人员可新增 / 修改产品（含上下架），数据落在数据库，无需改代码。</p>
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final DtoMapper dtoMapper;

    /** 当前登录人（未登录时为 null，产品页允许游客浏览） */
    private User currentUser() {
        var loginUser = SecurityUtils.current();
        if (loginUser == null || loginUser.getUserId() == null) {
            return null;
        }
        return userRepository.findById(loginUser.getUserId()).orElse(null);
    }

    public List<ProductDTO> list(String type, String sort) {
        User user = currentUser();
        List<Product> products;
        if (type != null && !type.isBlank() && !"全部".equals(type)) {
            products = productRepository.findByEnabledTrueAndTypeOrderByRateAsc(parseType(type));
        } else {
            products = productRepository.findByEnabledTrueOrderByRateAsc();
        }
        List<ProductDTO> list = products.stream().map(p -> dtoMapper.toProductDTO(p, user)).toList();
        return sortProducts(list, sort);
    }

    public List<ProductDTO> hotList() {
        User user = currentUser();
        return productRepository.findByEnabledTrueAndHotTrueOrderByApplyCountDesc()
                .stream().map(p -> dtoMapper.toProductDTO(p, user)).toList();
    }

    public ProductDTO detail(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("贷款产品不存在或已下架"));
        return dtoMapper.toProductDTO(product, currentUser());
    }

    /* ==================== 管理端：新增 / 修改 ==================== */

    /** 管理端产品列表：含已下架产品，供后台维护 */
    @Transactional(readOnly = true)
    public List<ProductDTO> listAll() {
        User user = currentUser();
        return productRepository.findAll().stream()
                .sorted(Comparator.comparing(Product::getId))
                .map(p -> dtoMapper.toProductDTO(p, user))
                .toList();
    }

    /** 新增产品：编号留空时按 P001 规则自动生成 */
    @OpLog(module = "产品管理", action = "新增贷款产品", target = "#request.name")
    @Transactional
    public ProductDTO create(ProductSaveRequest request) {
        require(request.getName(), "请输入产品名称");
        require(request.getType(), "请选择产品类型");
        if (request.getRate() == null) {
            throw new BusinessException("请输入年化利率");
        }
        if (request.getTermMonths() == null) {
            throw new BusinessException("请输入主推期限");
        }
        if (request.getMinAmount() == null) {
            throw new BusinessException("请输入起贷金额");
        }
        if (request.getMaxAmount() == null) {
            throw new BusinessException("请输入最高额度");
        }
        String id = request.getId() == null || request.getId().isBlank()
                ? nextId()
                : request.getId().trim();
        if (productRepository.existsById(id)) {
            throw new BusinessException("产品编号 " + id + " 已存在，请更换");
        }
        Product product = new Product();
        product.setId(id);
        product.setApplyCount(0);
        fill(product, request);
        productRepository.save(product);
        return dtoMapper.toProductDTO(product, currentUser());
    }

    /** 修改产品：未传的字段保持原值，改 enabled 即可完成上架 / 下架 */
    @OpLog(module = "产品管理", action = "修改贷款产品", target = "#id")
    @Transactional
    public ProductDTO update(String id, ProductSaveRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("贷款产品不存在：" + id));
        fill(product, request);
        productRepository.save(product);
        return dtoMapper.toProductDTO(product, currentUser());
    }

    /** 请求参数 -> 实体（增量语义：未传的字段保持原值，文本字段传空串即清空） */
    private void fill(Product product, ProductSaveRequest request) {
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getType() != null) {
            product.setType(parseType(request.getType().trim()));
        }
        if (request.getBadge() != null) {
            product.setBadge(trimToNull(request.getBadge()));
        }
        if (request.getIconText() != null) {
            product.setIconText(trimToNull(request.getIconText()));
        }
        if (request.getRate() != null) {
            product.setRate(request.getRate());
        }
        if (request.getTermMonths() != null) {
            product.setTermMonths(request.getTermMonths());
        }
        if (request.getTerms() != null) {
            product.setTerms(joinNumbers(request.getTerms()));
        }
        if (request.getMinAmount() != null) {
            product.setMinAmount(request.getMinAmount());
        }
        if (request.getMaxAmount() != null) {
            product.setMaxAmount(request.getMaxAmount());
        }
        if (product.getMinAmount() != null && product.getMaxAmount() != null
                && product.getMaxAmount() < product.getMinAmount()) {
            throw new BusinessException("最高额度不能小于起贷金额");
        }
        if (request.getRepayment() != null) {
            product.setRepayment(trimToNull(request.getRepayment()));
        }
        if (request.getGuarantee() != null) {
            product.setGuarantee(trimToNull(request.getGuarantee()));
        }
        if (request.getTarget() != null) {
            product.setTarget(trimToNull(request.getTarget()));
        }
        if (request.getFeatures() != null) {
            product.setFeatures(joinTexts(request.getFeatures()));
        }
        if (request.getHot() != null) {
            product.setHot(request.getHot());
        }
        if (request.getMinCreditLevel() != null) {
            product.setMinCreditLevel(parseLevel(request.getMinCreditLevel()));
        }
        if (request.getEnabled() != null) {
            product.setEnabled(request.getEnabled());
        }
    }

    /** 生成下一个产品编号，如已存在 P001~P006 则返回 P007 */
    private String nextId() {
        int max = 0;
        for (Product product : productRepository.findAll()) {
            String id = product.getId();
            if (id == null || !id.regionMatches(true, 0, "P", 0, 1)) {
                continue;
            }
            try {
                max = Math.max(max, Integer.parseInt(id.substring(1)));
            } catch (NumberFormatException ignored) {
                // 非 P + 数字 形式的编号不参与自增
            }
        }
        return "P" + String.format("%03d", max + 1);
    }

    /** 可选期限归一化：兼容 ["6","12"] 与 ["6,12,18"] */
    private String joinNumbers(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<String> numbers = new ArrayList<>();
        for (String value : values) {
            if (value == null) {
                continue;
            }
            for (String item : value.split("[,，;；\\s]+")) {
                String text = item.trim();
                if (text.isEmpty()) {
                    continue;
                }
                try {
                    numbers.add(String.valueOf(Integer.parseInt(text)));
                } catch (NumberFormatException e) {
                    throw new BusinessException("可选期限必须是数字，如 6,12,18");
                }
            }
        }
        return numbers.isEmpty() ? null : String.join(",", numbers);
    }

    /** 产品亮点归一化：分号分隔入库，兼容 ["A","B"] 与 ["A;B"] */
    private String joinTexts(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<String> items = new ArrayList<>();
        for (String value : values) {
            if (value == null) {
                continue;
            }
            for (String item : value.split("[;；\\n]+")) {
                String text = item.trim();
                if (!text.isEmpty()) {
                    items.add(text);
                }
            }
        }
        return items.isEmpty() ? null : String.join(";", items);
    }

    private String parseLevel(String level) {
        String value = level.trim().toUpperCase();
        if (!List.of("AAA", "AA", "A", "B", "C").contains(value)) {
            throw new BusinessException("准入门槛只能是 AAA、AA、A、B、C");
        }
        return value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
    }

    /** 首页统计：产品数量、累计申请次数、最低利率 */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> stats() {
        List<Product> products = productRepository.findByEnabledTrueOrderByRateAsc();
        double minRate = products.stream().mapToDouble(Product::getRate).min().orElse(0);
        int totalApply = products.stream().mapToInt(Product::getApplyCount).sum();
        return java.util.Map.of(
                "productCount", products.size(),
                "totalApply", totalApply,
                "minRate", minRate,
                "avgDays", 1.5);
    }

    private List<ProductDTO> sortProducts(List<ProductDTO> list, String sort) {
        if ("amount".equals(sort)) {
            list = list.stream().sorted((a, b) -> Long.compare(b.getMaxAmount(), a.getMaxAmount())).toList();
        } else if ("term".equals(sort)) {
            list = list.stream().sorted((a, b) -> Integer.compare(a.getTermMonths(), b.getTermMonths())).toList();
        } else {
            list = list.stream().sorted((a, b) -> Double.compare(a.getRate(), b.getRate())).toList();
        }
        return list;
    }

    private ProductType parseType(String type) {
        for (ProductType item : ProductType.values()) {
            if (item.getLabel().equals(type) || item.name().equalsIgnoreCase(type)) {
                return item;
            }
        }
        throw new BusinessException("未知产品类型：" + type);
    }
}
