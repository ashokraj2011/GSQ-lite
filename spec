
Spec: make the imported workflow qa-web-e2e-automation approvable
Hand this to Claude Code on the office laptop, together with the workflow JSON file.

What happened
The workflow was imported with singularity-flow workflow import <FILE>.json --confirm <PLAN> --propose. That created the proposal branch sflow/config-change/workflow/import-workflows-qa-web-e2e-automation-by7c4542da-346ceccc-568c58964c16.

singularity-flow configuration approve … refuses it (WORKFLOW_PROPOSAL_PROBLEMS, "Nothing was changed") because workflow validate finds two problems in the step "Web E2E test intake":

CHECK_TEMPLATE_CLAUSE_ANCHORS: The template of Web E2E test intake defines no requirement clauses.
CHECK_TEMPLATE_PLANNED_TABLE: The template of Web E2E test intake has no planned-claims table the kernel reads.
Why: the workflow has a code step, so SFlow requires planned claims. "Web E2E test intake" is both

the step that defines the requirement clauses a Story traces to, and
the step that plans what the code step must deliver.
Every Story would stop when that step is published, so approval refuses the workflow.

Goal
Produce a corrected copy of the workflow JSON whose "Web E2E test intake" template has:

placeholder clause anchors, and
the planned-claims table.
Import it as a new proposal and get configuration approve to accept it. Change nothing else.

Rules (do not break these)
Work on a copy of the JSON and keep the original untouched.
Do not bypass approval, and do not edit or push the sflow/config branch by hand.
Do not change other steps or other workflows. Do not delete the code step or switch planned claims off to make the check go away, unless the owner explicitly decides that.
Keep the template's existing text and only add what is missing. Template text is a per-Story placeholder, so use TODO lines and never write one Story's wording into it.
Do not put anything that looks like a password, token or key into the file, because the office GitHub push protection rejects it.
The JSON is checksum-protected, so the checksums must be recomputed exactly (step 4). Otherwise SFlow refuses the file (WORKFLOW_BUNDLE_ASSET_INVALID / WORKFLOW_BUNDLE_DIGEST_MISMATCH).
Steps
1. Withdraw the stuck proposal (in the repository where it was imported)
singularity-flow configuration proposals
singularity-flow configuration reject sflow/config-change/workflow/import-workflows-qa-web-e2e-automation-by7c4542da-346ceccc-568c58964c16 --confirm <REVISION-FROM-THE-LIST> --reason "intake template needs clause anchors and a planned-claims table"
singularity-flow configuration proposals        # the workflow proposal is gone
2. Find the template inside the JSON
The bundle has workflows, objects, assets, bundleSha256, …
In objects, find the workflow qa-web-e2e-automation and its step labelled "Web E2E test intake". Note which template that step uses.
In assets[], find the entry with "kind": "template" whose path / reference is that template. Its content is the Markdown to fix. Its size and sha256 must change with it.
3. Add the two missing pieces to that template's content
a) Clause anchors. At least one [{{work.id}}:REQ-001], written literally like that. It must sit outside HTML comments and code fences. If the template already has a requirements or acceptance section, put these lines there; otherwise add the section:

## Requirements
- TODO: one observable requirement per line. [{{work.id}}:REQ-001]
- TODO: one testable criterion per line. [{{work.id}}:AC-001]
b) Planned-claims table. The header must start exactly | Clause | Expected paths | Planned tests |. Use one row per REQ anchor in the template. If the template has a similar table with a different header, fix that table instead of adding a second one.

## Planned claims
| Clause | Expected paths | Planned tests | Fulfillment | Observable result |
|---|---|---|---|---|
| `{{work.id}}:REQ-001` | TODO: exact backticked source paths | TODO: exact backticked test paths | new | TODO: what a person observes |
Keep every heading the template already has. Publication of the step checks for its required sections.

4. Recompute the checksums
Write a small Node script that edits the copy:

asset.size = Buffer.byteLength(asset.content, 'utf8')
asset.sha256 = 'sha256:' + sha256hex(Buffer.from(asset.content, 'utf8'))
bundleSha256 = 'sha256:' + sha256hex(canonicalJson(bundle with the bundleSha256 field removed)). Here canonicalJson(v) is JSON.stringify of v, with every object's keys sorted alphabetically at every depth. Arrays keep their order, and there's no extra whitespace.
These match SFlow's own code. Confirm them in the installed SFlow package, src/workflow-transfer.mjs, functions canonicalValue, canonicalJson, digest, bundleWithoutDigest and assetBytes. To find the package, run npm root -g and look for the singularity-flow folder there.

Self-check first: run the recompute on the ORIGINAL file without editing it. It must reproduce the original bundleSha256 and the template asset's sha256 exactly. Only then apply it to the edited copy. Save the copy as UTF-8 JSON.

5. Import the corrected copy and approve it
singularity-flow workflow import <FIXED>.json --dry-run --json
singularity-flow workflow import <FIXED>.json --confirm sha256:<PLAN-SHA256-FROM-THE-PREVIEW> --propose --json
singularity-flow configuration proposals
singularity-flow configuration approve <NEW-PROPOSAL-BRANCH> --confirm <REVISION>
What each step tells you:
The dry run succeeding proves the checksums are right.
approve runs the same workflow validate check, so if anything is still wrong it refuses and changes nothing.
Run approve in the person's own terminal, not from a Copilot chat.
6. If approve still refuses
Read the refusal: it names the problem and the fix.
"The planned-claims table in the template … is refused": a cell or column is in the wrong shape. Match the header and the row in 3b exactly.
"The clause anchors in the template … are refused": an anchor is malformed. Use [{{work.id}}:REQ-001] with three digits.
Withdraw the new proposal (step 1 with its branch), fix the copy, recompute (step 4) and import again.
Done when
singularity-flow configuration proposals shows the workflow proposal as approved.
A new Story can select qa-web-e2e-automation.
Its "Web E2E test intake" draft contains the Requirements anchors and the Planned claims table for the author to fill in.
