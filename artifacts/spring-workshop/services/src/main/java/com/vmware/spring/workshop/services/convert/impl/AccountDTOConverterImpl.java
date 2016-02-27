package com.vmware.spring.workshop.services.convert.impl;

import java.lang.reflect.Method;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.vmware.spring.workshop.dao.api.BranchDao;
import com.vmware.spring.workshop.dao.api.UserDao;
import com.vmware.spring.workshop.dto.banking.AccountDTO;
import com.vmware.spring.workshop.model.banking.Account;
import com.vmware.spring.workshop.model.banking.Branch;
import com.vmware.spring.workshop.model.user.User;
import com.vmware.spring.workshop.services.convert.AccountDTOConverter;

/**
 * @author lgoldstein
 */
@Component("accountDTOConverter")
@Transactional
public class AccountDTOConverterImpl
		extends AbstractDTOConverter<Account,AccountDTO>
		implements AccountDTOConverter {
	private final BranchDao _daoBranch;
	private final UserDao	_daoUser;

	@Inject
	public AccountDTOConverterImpl (final BranchDao daoBranch,
									final UserDao	daoUser) {
		super(Account.class, AccountDTO.class);
		Assert.state((_daoBranch=daoBranch) != null, "No branch DAO provided"); 
		Assert.state((_daoUser=daoUser) != null, "No user DAO provided"); 
	}

	@Override
	protected ValueConverter<?, ?> resolveUnknownPropertyValueConverter(
					Class<?> srcClass, Method srcGetter, Method srcSetter,
					Class<?> dstClass, Method dstGetter, Method dstSetter) {
		final Class<?>	dstType=dstGetter.getReturnType();
		if (Branch.class.isAssignableFrom(dstType))
			return new ValueConverter<Long,Branch>() {
					@Override
					public Branch convertValue(Long srcValue) {
						if (srcValue == null)
							return null;
						else
							return _daoBranch.findOne(srcValue);
					}
				};
		else if (User.class.isAssignableFrom(dstType)) 
			return new ValueConverter<Long,User>() {
					@Override
					public User convertValue(Long srcValue) {
						if (srcValue == null)
							return null;
						else
							return _daoUser.findOne(srcValue);
					}
				};

		return super.resolveUnknownPropertyValueConverter(srcClass, srcGetter, srcSetter,
				  dstClass, dstGetter, dstSetter);
	}
}
