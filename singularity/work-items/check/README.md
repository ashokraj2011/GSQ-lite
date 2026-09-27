# check — chcek

Durable feature workflow state for branch `check`.

- [workflow.json](./workflow.json) — machine state and accepted workflow-snapshot reference
- [config/wfa/](./config/wfa/) — immutable effective policy, phase templates, and governed-agent bytes
- [STATUS.md](./STATUS.md) — human status
- [source.json](./source.json) — source context
- [USER-STORY.md](./USER-STORY.md) — manual story snapshot
- [documents.json](./documents.json) — supporting-document catalog (created on first upload)
- [inputs/](./inputs/) — uploaded files (created on first upload)
- [context/](./context/) — per-generation prompt-grounding audit records
- [telemetry/](./telemetry/) — sanitized per-generation model, token, and cost records
- [artifacts/](./artifacts/) — generated phase artifacts
- [approvals/](./approvals/) — append-only decisions
