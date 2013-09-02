/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.olingo.demo.jpa.controller;

import com.github.olingo.demo.jpa.controller.exceptions.IllegalOrphanException;
import com.github.olingo.demo.jpa.controller.exceptions.NonexistentEntityException;
import com.github.olingo.demo.jpa.controller.exceptions.PreexistingEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.github.olingo.demo.jpa.model.Customer;
import com.github.olingo.demo.jpa.model.MicroMarket;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author moscac
 */
public class MicroMarketJpaController implements Serializable {

    public MicroMarketJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MicroMarket microMarket) throws PreexistingEntityException, Exception {
        if (microMarket.getCustomerList() == null) {
            microMarket.setCustomerList(new ArrayList<Customer>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Customer> attachedCustomerList = new ArrayList<Customer>();
            for (Customer customerListCustomerToAttach : microMarket.getCustomerList()) {
                customerListCustomerToAttach = em.getReference(customerListCustomerToAttach.getClass(), customerListCustomerToAttach.getCustomerId());
                attachedCustomerList.add(customerListCustomerToAttach);
            }
            microMarket.setCustomerList(attachedCustomerList);
            em.persist(microMarket);
            for (Customer customerListCustomer : microMarket.getCustomerList()) {
                MicroMarket oldZipOfCustomerListCustomer = customerListCustomer.getZip();
                customerListCustomer.setZip(microMarket);
                customerListCustomer = em.merge(customerListCustomer);
                if (oldZipOfCustomerListCustomer != null) {
                    oldZipOfCustomerListCustomer.getCustomerList().remove(customerListCustomer);
                    oldZipOfCustomerListCustomer = em.merge(oldZipOfCustomerListCustomer);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMicroMarket(microMarket.getZipCode()) != null) {
                throw new PreexistingEntityException("MicroMarket " + microMarket + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MicroMarket microMarket) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            MicroMarket persistentMicroMarket = em.find(MicroMarket.class, microMarket.getZipCode());
            List<Customer> customerListOld = persistentMicroMarket.getCustomerList();
            List<Customer> customerListNew = microMarket.getCustomerList();
            List<String> illegalOrphanMessages = null;
            for (Customer customerListOldCustomer : customerListOld) {
                if (!customerListNew.contains(customerListOldCustomer)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Customer " + customerListOldCustomer + " since its zip field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Customer> attachedCustomerListNew = new ArrayList<Customer>();
            for (Customer customerListNewCustomerToAttach : customerListNew) {
                customerListNewCustomerToAttach = em.getReference(customerListNewCustomerToAttach.getClass(), customerListNewCustomerToAttach.getCustomerId());
                attachedCustomerListNew.add(customerListNewCustomerToAttach);
            }
            customerListNew = attachedCustomerListNew;
            microMarket.setCustomerList(customerListNew);
            microMarket = em.merge(microMarket);
            for (Customer customerListNewCustomer : customerListNew) {
                if (!customerListOld.contains(customerListNewCustomer)) {
                    MicroMarket oldZipOfCustomerListNewCustomer = customerListNewCustomer.getZip();
                    customerListNewCustomer.setZip(microMarket);
                    customerListNewCustomer = em.merge(customerListNewCustomer);
                    if (oldZipOfCustomerListNewCustomer != null && !oldZipOfCustomerListNewCustomer.equals(microMarket)) {
                        oldZipOfCustomerListNewCustomer.getCustomerList().remove(customerListNewCustomer);
                        oldZipOfCustomerListNewCustomer = em.merge(oldZipOfCustomerListNewCustomer);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = microMarket.getZipCode();
                if (findMicroMarket(id) == null) {
                    throw new NonexistentEntityException("The microMarket with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            MicroMarket microMarket;
            try {
                microMarket = em.getReference(MicroMarket.class, id);
                microMarket.getZipCode();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The microMarket with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Customer> customerListOrphanCheck = microMarket.getCustomerList();
            for (Customer customerListOrphanCheckCustomer : customerListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This MicroMarket (" + microMarket + ") cannot be destroyed since the Customer " + customerListOrphanCheckCustomer + " in its customerList field has a non-nullable zip field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(microMarket);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MicroMarket> findMicroMarketEntities() {
        return findMicroMarketEntities(true, -1, -1);
    }

    public List<MicroMarket> findMicroMarketEntities(int maxResults, int firstResult) {
        return findMicroMarketEntities(false, maxResults, firstResult);
    }

    private List<MicroMarket> findMicroMarketEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MicroMarket.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public MicroMarket findMicroMarket(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MicroMarket.class, id);
        } finally {
            em.close();
        }
    }

    public int getMicroMarketCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MicroMarket> rt = cq.from(MicroMarket.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
