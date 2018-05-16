package com.ustcinfo.ptp.yunting.service;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.engine.jdbc.LobCreator;

import com.starit.common.dao.hibernate4.HibernateBaseDao;
import com.starit.common.dao.support.Pagination;
import com.starit.common.dao.support.PaginationRequest;

public interface YuntingBaseService<T, ID extends Serializable>{
public List<T> findAllEntity();
	
	public HibernateBaseDao<T, ID> getHibernateBaseDao();
	
	public T getEntity(ID id);
	
	public ID insertEntity(T entity);
	
	public void updateEntity(T entity);
	
	public void createOrUpdate(T entity);
	
	public T deleteEntity(ID id);
	
	public T logicDeleteEntity(ID id);
	
	public void bulkDeleteEntity(ID[] ids);
	
	public List<T> loadEntities();
	
	public List<T> findByNamedParam(String propertyName, Object value);
	
	public List<T> findByNamedParam(String[] propertyNames, Object[] values);
	
	public List<T> findByNamedParamAndOrder(String propertyName, Object value, Order order);
	
	public List<T> findByNamedParamAndOrder(String[] propertyNames, Object[] values, Order ... orders);
	
	public List<T> findByNamedParamAndOrder(String joinEntity, String propertyName, Object value, Order order);
	
	public List<T> findByNamedParamAndOrder(String[] joinEntitys, String propertyName, Object value, Order order);
	
	public List<T> findByNamedParamAndOrder(String[] joinEntitys, String[] propertyNames, Object[] values, Order order);
	
	public Pagination<T> findPage(PaginationRequest<T> paginationRequest);
	
	public LobCreator getLobCreator();
}
