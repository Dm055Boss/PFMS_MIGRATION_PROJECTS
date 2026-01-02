package com.intech.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TXN generation properties (XML attributes, output paths, date logic, defaults).
 *
 * All fields are sourced from external properties (appn.props).
 */
@ConfigurationProperties(prefix = "txn")
public class TxnProperties {

    private final Date date = new Date();
    private final Output output = new Output();
    private final Bank bank = new Bank();
    private final Root root = new Root();
    private final TxnDefaults txn = new TxnDefaults();

    /**
     * Sequence appended to MessageId and file name (e.g., ...041220251).
     * If you need a dynamic sequence later, we can replace this with a DB/FS counter.
     */
    private int fileSeq = 1;

    public Date getDate() { return date; }

    public Output getOutput() { return output; }

    public Bank getBank() { return bank; }

    public Root getRoot() { return root; }

    public TxnDefaults getTxn() { return txn; }

    public int getFileSeq() { return fileSeq; }

    public void setFileSeq(int fileSeq) { this.fileSeq = fileSeq; }

    public static class Date {
        /**
         * txnDate = today(Asia/Kolkata) + offsetDays
         */
        private int offsetDays = -1;

        /**
         * Timezone for date computations and scheduler.
         */
        private String zone = "Asia/Kolkata";

        public int getOffsetDays() { return offsetDays; }

        public void setOffsetDays(int offsetDays) { this.offsetDays = offsetDays; }

        public String getZone() { return zone; }

        public void setZone(String zone) { this.zone = zone; }
    }

    public static class Output {
        /**
         * Output directory where XML files will be generated.
         */
        private String dir;

        /**
         * Whether output directory should be created if missing.
         */
        private boolean createDir = true;

        public String getDir() { return dir; }

        public void setDir(String dir) { this.dir = dir; }

        public boolean isCreateDir() { return createDir; }

        public void setCreateDir(boolean createDir) { this.createDir = createDir; }
    }

    public static class Bank {
        private String code;
        private String name;

        public String getCode() { return code; }

        public void setCode(String code) { this.code = code; }

        public String getName() { return name; }

        public void setName(String name) { this.name = name; }
    }

    public static class Root {
        private String destination = "CPSMS";
        private String source;
        /**
         * Root attribute RecordsCount. If <= 0, it will be set to the actual number of processed accounts.
         */
        private int recordsCount = 1000;

        public String getDestination() { return destination; }

        public void setDestination(String destination) { this.destination = destination; }

        public String getSource() { return source; }

        public void setSource(String source) { this.source = source; }

        public int getRecordsCount() { return recordsCount; }

        public void setRecordsCount(int recordsCount) { this.recordsCount = recordsCount; }
    }

    public static class TxnDefaults {
        private String channelDefault = "V";
        private String postTranBalDefault = "";
        private String drcrAccountNoDefault = "";
        private String drcrAccountNameDefault = "";
        private String drcrBankNameDefault = "";
        private String drcrBankBranchCodeDefault = "";
        private String tranRefNoDefault = "";
        private String cpsmsTransactionIdDefault = "";

        public String getChannelDefault() { return channelDefault; }

        public void setChannelDefault(String channelDefault) { this.channelDefault = channelDefault; }

        public String getPostTranBalDefault() { return postTranBalDefault; }

        public void setPostTranBalDefault(String postTranBalDefault) { this.postTranBalDefault = postTranBalDefault; }

        public String getDrcrAccountNoDefault() { return drcrAccountNoDefault; }

        public void setDrcrAccountNoDefault(String drcrAccountNoDefault) { this.drcrAccountNoDefault = drcrAccountNoDefault; }

        public String getDrcrAccountNameDefault() { return drcrAccountNameDefault; }

        public void setDrcrAccountNameDefault(String drcrAccountNameDefault) { this.drcrAccountNameDefault = drcrAccountNameDefault; }

        public String getDrcrBankNameDefault() { return drcrBankNameDefault; }

        public void setDrcrBankNameDefault(String drcrBankNameDefault) { this.drcrBankNameDefault = drcrBankNameDefault; }

        public String getDrcrBankBranchCodeDefault() { return drcrBankBranchCodeDefault; }

        public void setDrcrBankBranchCodeDefault(String drcrBankBranchCodeDefault) { this.drcrBankBranchCodeDefault = drcrBankBranchCodeDefault; }

        public String getTranRefNoDefault() { return tranRefNoDefault; }

        public void setTranRefNoDefault(String tranRefNoDefault) { this.tranRefNoDefault = tranRefNoDefault; }

        public String getCpsmsTransactionIdDefault() { return cpsmsTransactionIdDefault; }

        public void setCpsmsTransactionIdDefault(String cpsmsTransactionIdDefault) { this.cpsmsTransactionIdDefault = cpsmsTransactionIdDefault; }
    }
}
