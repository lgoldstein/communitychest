package com.vmware.spring.workshop.facade.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author lgoldstein
 */
public interface CSVImportExport<DTO> {
    static final String    LINE_SEP="\r\n";    // see RFC4180

    Class<DTO> getDTOClass ();

    // returns null of no more data
    DTO toDTO (BufferedReader reader, List<String> propsOrder) throws IOException;
    DTO toDTO (String data, List<String> propsOrder) throws IOException;

    <A extends Appendable> A appendTitleLine (A sb) throws IOException;
    <A extends Appendable> A appendTitleLine (A sb, List<String> propsOrder) throws IOException;

    <A extends Appendable> A appendDTO (A sb, DTO dto) throws IOException;
    <A extends Appendable> A appendDTO (A sb, DTO dto, List<String> propsOrder) throws IOException;

    <A extends Appendable> A appendDTOList (A sb, Collection<? extends DTO> dtoList) throws IOException;
    <A extends Appendable> A appendDTOList (A sb, List<String> propsOrder, Collection<? extends DTO> dtoList) throws IOException;
}
