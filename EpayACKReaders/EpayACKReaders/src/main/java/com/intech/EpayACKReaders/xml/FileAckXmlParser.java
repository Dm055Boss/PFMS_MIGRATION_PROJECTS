// src/main/java/com/intech/epayackreader/xml/FileAckXmlParser.java
package com.intech.EpayACKReaders.xml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.intech.EpayACKReaders.bean.FileAck;
import com.intech.EpayACKReaders.exceptions.XmlParsingException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

/**
 * Parses ACK XML into FileAck object using JAXB.
 */
@Component
public class FileAckXmlParser {

    private static final Logger LOGGER = LogManager.getLogger(FileAckXmlParser.class);

    private final JAXBContext jaxbContext;

    public FileAckXmlParser() {
        try {
            this.jaxbContext = JAXBContext.newInstance(FileAck.class);
            LOGGER.info("Initialized JAXBContext for FileAck.");
        } catch (JAXBException e) {
            LOGGER.error("Failed to initialize JAXBContext for FileAck", e);
            throw new IllegalStateException("Cannot initialize JAXBContext for FileAck", e);
        }
    }

    /**
     * Parses the given XML file into FileAck.
     *
     * @param path XML file path
     * @return parsed FileAck object
     */
    public FileAck parse(Path path) {
        LOGGER.debug("Parsing ACK XML file: {}", path);
        try (InputStream is = Files.newInputStream(path)) {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Object result = unmarshaller.unmarshal(is);
            if (result instanceof FileAck fileAck) {
                return fileAck;
            } else {
                throw new XmlParsingException("Unexpected JAXB result type: " + result.getClass(), path.toString());
            }
        } catch (JAXBException e) {
            LOGGER.error("JAXBException while parsing ACK XML file: {}", path, e);
            throw new XmlParsingException("Failed to unmarshal XML", path.toString(), e);
        } catch (IOException e) {
            LOGGER.error("IOException while reading ACK XML file: {}", path, e);
            throw new XmlParsingException("Failed to read XML file", path.toString(), e);
        }
    }
}
