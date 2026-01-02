# txn-reporting-service

## What this does
Scheduled Spring Boot JAR that:
1) Picks eligible accounts: `SELECT DISTINCT ACCT_NO FROM TXN_ACCOUNT_STATUS WHERE DATA_REQ_FLAG='Y'`
2) For each account, loads Account tag attributes using BAL query (GAM+SOL with stored balances)
3) Loads transaction count (CNT query)
4) If count > 0, loads transaction details (TXN query) and writes `<Transaction>` nodes
5) Generates one TXN XML file per run in a configured output directory

## Run requirements
- Java 17+
- Oracle DB reachable
- External runtime properties file (`appn.props`)
- External SQL queries file (`queries.properties`)
- Output folder exists and is writable

## Build
mvn clean package

## Run
java -jar target/txn-reporting-service-1.0.0.jar

## External runtime configuration (example)

### application.properties (inside jar)
Contains only:
- appn.props.path
- spring.config.import=optional:file:${appn.props.path}

### appn.props (external)
See `config-samples/appn.props`

### queries.properties (external)
See `config-samples/queries.properties`

> Important: This service supports BAL query with named parameters (:txndate, :foracid) and CNT/TXN queries with positional '?' placeholders.

