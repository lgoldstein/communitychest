/*
 * 
 */
package net.community.chest.spring.test.beans;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import net.community.chest.spring.test.entities.DateTimeEntity;

/**
 * @author Lyor G.
 * @since Jul 20, 2010 8:54:38 AM
 */
public interface TestBeanService extends Appendable {
	TestBeanService println () throws IOException;

	List<DateTimeEntity> listDateTimeEntities (Collection<DateTimeEntitySortOrder> listOrder);
	List<DateTimeEntity> listDateTimeEntities (DateTimeEntitySortOrder ... listOrder);

	List<DateTimeEntity> findDateTimeEntity (Collection<DateTimeEntitySortOrder> listOrder, Collection<Long> ids);
	List<DateTimeEntity> findDateTimeEntity (Collection<DateTimeEntitySortOrder> listOrder, Long ... ids);

	DateTimeEntity createDateTimeEntity (String name, long timestampValue);
	void updateDateTimeEntity (DateTimeEntity dte);

	DateTimeEntity deleteDateTimeEntity (Long id);
	void deleteDateTimeEntity (DateTimeEntity dte);
}
