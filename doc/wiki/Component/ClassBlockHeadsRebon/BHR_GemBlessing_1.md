# 宝石祝福：紫锂辉（NBT）
{`GemBlessing`}（NBT 键，int，非注册名）

> [!DANGER] 重要信息
> 该Java类即将弃用，重制后该Java类将不再存在，其中包含的内容将被放置在更合适的地方
> - 宝石祝福：紫锂辉（NBT） → 宝石祝福（组件）[[BHR_GemBlessing]]

## 备注
- 现状为**原始 NBT 实现**：在物品上写入整数键 `GemBlessing`，当前仅测试写入值 `1`（紫锂辉祝福）
- 该实现属于物品 NBT 时代；重制后由 [[BHR_GemBlessing]] 的组件方案取代
- 2026-10：[[BHR_GemBlessing]] 已落地（组件 + 三处 `EnchantmentHelper` 注入），本节所述的
  NBT 方案不再参与编译（类已移入 `src/main/javaref/`，其旧 mixin 也已从 mixin 配置中移除）。
  **存档不做迁移** —— 这是重置而非迁移。
