package py.com.vsantat.dof;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;
import py.com.vsantat.core.util.ApplicationException;
import py.com.vsantat.core.util.TimeRange;

public abstract class RecordBeanController<T extends RecordBean> implements Serializable {

	private static final long serialVersionUID = 6528953466807677895L;

	Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	public static enum OrderDirection {
		NONE, ASC, DSC
	};

	public static enum GroupingMethod {
		COUNT, SUM, COUNT_UNIQUE
	};

	protected Class<T> entityClass;

	@PersistenceContext(unitName = "intnLabelsUnit")
	private EntityManager entityManager;

	@Resource
	private UserTransaction transaction;

	public T createRecord() {
		T record = null;
		try {
			record = (T) getEntityClass().getConstructor().newInstance();
		} catch (Exception e) {
			logger.severe(e.getMessage());
		}
		return (T) record;
	}

	public T getRecord(long id) {
		return (T) getRecord(id, false);
	}

	public T getRecord(long id, boolean refresh) {
		T record = null;
		try {
			record = (T) entityManager.find(getEntityClass(), id);
			if (refresh) {
				entityManager.refresh(record);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
		} finally {
			entityManager.clear();

		}
		return (T) record;
	}

	public T getRecordByCode(String code) {
		return (T) getRecordByCode(code, false);
	}

	public T getRecordByCode(String code, boolean refresh) {
		T record = null;
		try {
			record = getRecord("code", code, refresh);
		} catch (Exception e) {
			logger.severe(e.getMessage());
		}
		return (T) record;
	}

	public T getRecord(String fieldName, Object fieldValue) {
		return getRecord(fieldName, fieldValue, false);
	}

	@SuppressWarnings("unchecked")
	public T getRecord(String fieldName, Object fieldValue, boolean refresh) {
		T record = null;
		try {
			String queryString = "SELECT O FROM " + getEntityClass().getSimpleName() + " O WHERE O." + fieldName + " = :" + fieldName;
			Query query = entityManager.createQuery(queryString, getEntityClass());
			query.setParameter(fieldName, fieldValue);
			record = (T) query.getSingleResult();
			if (refresh) {
				entityManager.refresh(record);
			}
		} catch (NoResultException e) {
			logger.fine(getEntityClass().getSimpleName() + " no posee un registro con el valor: '" + fieldValue + "' en el campo: " + fieldName);
		} catch (NonUniqueResultException e) {
			logger.log(Level.SEVERE, "Se encontro mas de un registro en: " + getEntityClass().getSimpleName() + "  con el valor: '" + fieldValue + "' en el campo: " + fieldName);
		} finally {
			entityManager.clear();

		}
		return (T) record;
	}

	public T getSingleResult(Map<String, Object> filters) {
		return (T) getSingleResult(filters, null);
	}

	public Query getQuery(Map<String, Object> filters, Map<String, OrderDirection> orderFields) {
		return getQuery(entityManager, filters, orderFields);
	}

	public Query getQuery(EntityManager entityManager, Map<String, Object> filters, Map<String, OrderDirection> orderFields) {
		String queryString = "SELECT O FROM " + getEntityClass().getSimpleName() + " O";
		if ((filters != null) && (filters.size() > 0)) {
			queryString += " WHERE";
			for (String filterKey : filters.keySet()) {
				if (filters.get(filterKey) instanceof List<?>) {
					queryString += " O." + filterKey + " IN :" + filterKey + " AND";
				}
				if (filters.get(filterKey) instanceof TimeRange) {
					TimeRange timeRange = (TimeRange) filters.get(filterKey);
					if (timeRange.getStart() != null) {
						queryString += " O." + filterKey + " >= :" + filterKey + "_START AND";
					}
					if (timeRange.getEnd() != null) {
						queryString += " O." + filterKey + " <= :" + filterKey + "_END AND";
					}
				} else if (filters.get(filterKey) == null) {
					queryString += " O." + filterKey + " IS NULL AND";
				} else {
					queryString += " O." + filterKey + " = :" + filterKey + " AND";
				}
			}
			if (queryString.endsWith(" AND")) {
				queryString = queryString.substring(0, queryString.lastIndexOf(" AND"));
			}
		}
		if ((orderFields != null) && (orderFields.size() > 0)) {
			queryString += " ORDER BY";
			for (String orderKey : orderFields.keySet()) {
				queryString += " O." + orderKey + " ";
				OrderDirection direction = orderFields.get(orderKey);
				if (direction == OrderDirection.ASC) {
					queryString += " ASC,";
				} else {
					queryString += " DESC,";
				}
			}
		}
		if (queryString.endsWith(",")) {
			queryString = queryString.substring(0, queryString.lastIndexOf(","));
		}

		Query query = entityManager.createQuery(queryString, getEntityClass());
		if ((filters != null) && (filters.size() > 0)) {
			for (String filterKey : filters.keySet()) {
				Object object = filters.get(filterKey);
				if (object != null) {
					if (object instanceof TimeRange) {
						TimeRange timeRange = (TimeRange) object;
						if (timeRange.getStart() != null) {
							query.setParameter(filterKey + "_START", timeRange.getStart());
						}
						if (timeRange.getEnd() != null) {
							query.setParameter(filterKey + "_END", timeRange.getEnd());
						}
					} else {
						query.setParameter(filterKey, filters.get(filterKey));
					}
				}
			}
		}

		return query;
	}

	public Query getCountQuery(Map<String, Object> filters) {
		return getCountQuery(entityManager, filters);
	}

	public Query getCountQuery(EntityManager entityManager, Map<String, Object> filters) {
		String queryString = "SELECT count(O.id) FROM " + getEntityClass().getSimpleName() + " O";
		if ((filters != null) && (filters.size() > 0)) {
			queryString += " WHERE";
			for (String filterKey : filters.keySet()) {
				if (filters.get(filterKey) instanceof List<?>) {
					queryString += " O." + filterKey + " IN :" + filterKey + " AND";
				} else if (filters.get(filterKey) == null) {
					queryString += " O." + filterKey + " IS NULL AND";
				} else {
					queryString += " O." + filterKey + " = :" + filterKey + " AND";
				}
			}
			if (queryString.endsWith(" AND")) {
				queryString = queryString.substring(0, queryString.lastIndexOf(" AND"));
			}
		}
		if (queryString.endsWith(",")) {
			queryString = queryString.substring(0, queryString.lastIndexOf(","));
		}
		Query query = entityManager.createQuery(queryString, Long.class);
		if ((filters != null) && (filters.size() > 0)) {
			for (String filterKey : filters.keySet()) {
				if (filters.get(filterKey) != null) {
					query.setParameter(filterKey, filters.get(filterKey));
				}
			}
		}

		return query;
	}

	@SuppressWarnings("unchecked")
	public T getSingleResult(Map<String, Object> filters, Map<String, OrderDirection> orderFields) {
		T record = null;
		try {
			Query query = getQuery(filters, orderFields);
			try {
				record = (T) query.getSingleResult();
			} catch (NoResultException e) {// No hay resultado, entonces null
				record = null;
			}
		} finally {
			entityManager.clear();
		}
		return (T) record;
	}

	public List<T> getRecordList() {
		List<T> recordList = new ArrayList<>();
		try {
			String queryString = "SELECT O FROM " + getEntityClass().getSimpleName() + " O";
			List<?> resultList = entityManager.createQuery(queryString, getEntityClass()).getResultList();
			Iterator<?> iterator = resultList.iterator();
			while (iterator.hasNext()) {
				@SuppressWarnings("unchecked")
				T recordBean = (T) iterator.next();
				recordList.add(recordBean);
			}
		} finally {
			entityManager.clear();
		}
		return recordList;
	}

	public List<T> getRecordList(Map<String, Object> filters) {
		return (List<T>) getRecordList(filters, null);
	}

	public List<T> getRecordList(Map<String, Object> filters, Map<String, OrderDirection> orderFields) {
		List<T> recordList = new ArrayList<>();
		try {
			Query query = getQuery(filters, orderFields);
			List<?> resultList = query.getResultList();
			Iterator<?> iterator = resultList.iterator();
			while (iterator.hasNext()) {
				@SuppressWarnings("unchecked")
				T recordBean = (T) iterator.next();
				recordList.add(recordBean);
			}

		} finally {
			entityManager.clear();
		}
		return recordList;
	}

	public Long getRecordCount(Map<String, Object> filters) {
		Long result = 0l;
		if (filters.size() > 0) {
			try {
				Query query = getCountQuery(filters);
				result = (Long) query.getSingleResult();
			} catch (Exception e) {
				logger.warning("No se encontraron resultados en el conteo de " + getEntityClass().getSimpleName() + ":" + e.getMessage());
			} finally {
				entityManager.clear();

			}
		}
		return result;
	}

	public T save(T record) {
		try {
			transaction.begin();
			if (record.isNew()) {
				entityManager.persist(record);
			} else {
				record = entityManager.merge(record);
			}
			entityManager.flush();
			transaction.commit();
		} catch (Exception e) {
			record = null;
			logger.severe(e.getMessage());
		} finally {
			entityManager.clear();

		}
		return record;
	}

	public void save(List<T> recordList) throws ApplicationException {// TODO: move this code to JPATable bulk save
																		// function
		try {
			transaction.begin();
			int count = 0;
			for (T dataRecord : recordList) {
				if (dataRecord == null) {
					continue;
				}
				count++;
				if (dataRecord.isNew()) {
					entityManager.persist(dataRecord);
				} else {
					entityManager.merge(dataRecord);
				}
				if (count == 500) {
					entityManager.flush();
					entityManager.clear();
					count = 0;
				}
			}
			entityManager.flush();
			entityManager.clear();
			transaction.commit();
		} catch (Exception e) {
			logger.severe("Error en transaccion " + e.getMessage());
			throw new ApplicationException("Error al guardar los cambios. Razon:" + e.getMessage());
		}

	}

	public void delete(T record) {
		try {
			transaction.begin();
			record = entityManager.merge(record);
			entityManager.remove(record);
			transaction.commit();
		} catch (Exception e) {
			logger.severe("Error en transaccion " + e.getMessage());
		} finally {
			entityManager.clear();

		}
	}

	public void bulkDelete(String fieldName, Object fieldValue) {
		try {
			transaction.begin();
			String queryString = "DELETE FROM " + getEntityClass().getSimpleName() + " O WHERE O." + fieldName + " = :" + fieldName;
			Query query = entityManager.createQuery(queryString);
			query.setParameter(fieldName, fieldValue);
			query.executeUpdate();
			transaction.commit();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "No se pudieron eliminar los datos en la tabla" + getEntityClass().getSimpleName());
		} finally {
			entityManager.clear();

		}
	}

	public void bulkDelete(Map<String, Object> filters) {
		if ((filters != null) && (filters.size() > 0)) {
			try {
				transaction.begin();
				String queryString = "DELETE FROM " + getEntityClass().getSimpleName() + " O WHERE";
				for (String filterKey : filters.keySet()) {
					if (filters.get(filterKey) instanceof List<?>) {
						queryString += " O." + filterKey + " IN :" + filterKey + " AND";
					} else if (filters.get(filterKey) == null) {
						queryString += " O." + filterKey + " IS NULL AND";
					} else {
						queryString += " O." + filterKey + " = :" + filterKey + " AND";
					}
				}
				if (queryString.endsWith(" AND")) {
					queryString = queryString.substring(0, queryString.lastIndexOf(" AND"));
				}
				Query query = entityManager.createQuery(queryString);
				for (String filterKey : filters.keySet()) {
					if (filters.get(filterKey) != null) {
						query.setParameter(filterKey, filters.get(filterKey));
					}
				}
				query.executeUpdate();
				transaction.commit();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "No se pudieron eliminar los datos en la tabla" + getEntityClass().getSimpleName());
			} finally {
				entityManager.clear();

			}
		}
	}

	@SuppressWarnings("unchecked")
	public Class<T> getEntityClass() {
		if (entityClass == null) {
			entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		}
		return entityClass;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}
}
