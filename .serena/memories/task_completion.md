# 完成任务后的检查建议

- 后端：`mvn test`（或至少运行相关单测/集成测试）
- 前端：`npm run lint` + `npm run build`（如改动前端）
- 接口联调：验证 /api/ai/diagram 与 /api/ai/diagram/edit