package com.intech.EpayRejFileGenerator.xml;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.intech.EpayRejFileGenerator.bean.RejectedPayments;
import com.intech.EpayRejFileGenerator.config.RejectAckProperties;
import com.intech.EpayRejFileGenerator.exception.RejectAckException;
import com.intech.EpayRejFileGenerator.model.RejectedCreditRecord;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

/**
 * Builds and writes reject XML using the JAXB bean.RejectedPayments.
 */
@Component
public class RejectXmlWriter {

    private static final Logger log = LogManager.getLogger(RejectXmlWriter.class);

    private final RejectAckProperties props;

    public RejectXmlWriter(RejectAckProperties props) {
        this.props = props;
    }

    /**
     * Creates XML for one batch and writes it to disk.
     *
     * @return path of the generated file
     */
    public Path writeRejectXml(String messageId,
                               String paymentProduct,
                               String bankCode,
                               String bankName,
                               String destination,
                               String source,
                               String cpsmsBatchNo,
                               BigDecimal totalAmount,
                               int recordCount,
                               List<RejectedCreditRecord> credits) {

        try {
            RejectedPayments root = new RejectedPayments();

            // Header attributes
            root.setMessageId(messageId);
            root.setPaymentProduct(paymentProduct);
            root.setBankCode(bankCode);
            root.setBankName(bankName);
            root.setDestination(destination);
            root.setSource(source);
            root.setRecordsCount("1");  // one BatchDetails per file

            // BatchDetails
            RejectedPayments.BatchDetails batchDetails = new RejectedPayments.BatchDetails();

            DecimalFormat df = new DecimalFormat("0.00");
            String sumStr = df.format(totalAmount);
            batchDetails.setC4115(sumStr);
            batchDetails.setC5185(String.valueOf(recordCount));
            batchDetails.setCPSMSBatchNo(cpsmsBatchNo);

            // Add each rejected credit as RejectedPayment
            for (RejectedCreditRecord rec : credits) {
                RejectedPayments.BatchDetails.RejectedPayment xmlPay =
                        new RejectedPayments.BatchDetails.RejectedPayment();

                xmlPay.setC2020(rec.getC2020());
                xmlPay.setC5756(rec.getC5756());
                xmlPay.setC2006(rec.getC2006());
                xmlPay.setC5569(rec.getC5569());
                xmlPay.setC6061(rec.getC6061());
                xmlPay.setUID(rec.getUid() == null ? "" : rec.getUid());
                xmlPay.setBankIIN(rec.getBankIIN() == null ? "" : rec.getBankIIN());
                xmlPay.setC6081(rec.getC6081());
                xmlPay.setC5565(rec.getC5565());

                // Amount as string without scientific notation
                String amountStr = rec.getAmount() == null
                        ? "0"
                        : rec.getAmount().stripTrailingZeros().toPlainString();
                xmlPay.setC4038(amountStr);

                xmlPay.setC3380(rec.getC3380());
                xmlPay.setC3375(rec.getC3375());
                xmlPay.setC3381(rec.getC3381());
                xmlPay.setC6346(rec.getC6346());
                xmlPay.setC6366(rec.getC6366());
                xmlPay.setC7495(rec.getC7495());
                xmlPay.setPmtRoute(rec.getPmtRoute());

                batchDetails.getRejectedPayment().add(xmlPay);
            }

            root.setBatchDetails(batchDetails);

            // Prepare output directory and file
            Path dir = Paths.get(props.getOutputDir());
            Files.createDirectories(dir);

            Path filePath = dir.resolve(messageId + ".xml");
            File file = filePath.toFile();

            // Marshal JAXB object into XML file
            JAXBContext jaxbContext = JAXBContext.newInstance(RejectedPayments.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(root, file);

            // Set POSIX permissions if supported (rwx for owner/group/others)
            try {
                Set<PosixFilePermission> perms = new HashSet<>();
                perms.add(PosixFilePermission.OWNER_READ);
                perms.add(PosixFilePermission.OWNER_WRITE);
                perms.add(PosixFilePermission.OWNER_EXECUTE);
                perms.add(PosixFilePermission.GROUP_READ);
                perms.add(PosixFilePermission.GROUP_WRITE);
                perms.add(PosixFilePermission.GROUP_EXECUTE);
                perms.add(PosixFilePermission.OTHERS_READ);
                perms.add(PosixFilePermission.OTHERS_WRITE);
                perms.add(PosixFilePermission.OTHERS_EXECUTE);

                Files.setPosixFilePermissions(filePath, perms);
            } catch (UnsupportedOperationException uoe) {
                // Windows or non-POSIX FS – log and ignore
                log.warn("POSIX file permissions not supported on this platform. File={}", filePath);
            } catch (Exception ex) {
                log.warn("Failed to set POSIX permissions on file={}", filePath, ex);
            }

            log.info("Reject XML generated: {}", filePath);
            return filePath;

        } catch (Exception ex) {
            log.error("Error while generating Reject XML for messageId={}", messageId, ex);
            throw new RejectAckException("Failed to generate reject XML for messageId=" + messageId, ex);
        }
    }
}
