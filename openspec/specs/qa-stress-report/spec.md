# qa-stress-report Specification

## Purpose
TBD - created by archiving change qa-stress-develop. Update Purpose after archive.
## Requirements
### Requirement: QA report for develop is produced
The system SHALL produce a QA report for the `develop` branch that records environment, commands executed, results, issues, and a release recommendation.

#### Scenario: QA report file created
- **WHEN** QA and stress tests complete on `develop`
- **THEN** a report is created at `docs/qa/QA-YYYY-MM-DD-develop.md` with the required sections

### Requirement: Aggressive stress test is executed and recorded
The system SHALL execute an aggressive stress test against the API and record the outcome in the QA report.

#### Scenario: Stress test results captured
- **WHEN** the stress test runs with >=50 VUs for >=2 minutes
- **THEN** the report includes summary metrics (error rate, p95, throughput) and the exact command used

### Requirement: UI QA covers client and kitchen flows
The system SHALL validate the client and kitchen UI flows and record evidence in the QA report.

#### Scenario: UI flow evidence recorded
- **WHEN** a tester creates an order in the client UI and updates status in kitchen UI
- **THEN** the report records the flow steps and a sample orderId

### Requirement: RabbitMQ verification is documented
The system SHALL verify RabbitMQ event flow and record the evidence in the QA report.

#### Scenario: RabbitMQ evidence captured
- **WHEN** an order is created and processed
- **THEN** the report includes queue inspection and worker log evidence

