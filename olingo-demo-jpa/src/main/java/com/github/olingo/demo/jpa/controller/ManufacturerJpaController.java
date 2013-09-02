/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.olingo.demo.jpa.controller;

import com.github.olingo.demo.jpa.controller.exceptions.IllegalOrphanException;
import com.github.olingo.demo.jpa.controller.exceptions.NonexistentEntityException;
import com.github.olingo.demo.jpa.controller.exceptions.PreexistingEntityException;
import com.github.olingo.demo.jpa.model.Manufacturer;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.github.olingo.demo.jpa.model.Product;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author moscac
 */
public class ManufacturerJpaController implements Serializable {

    public ManufacturerJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Manufacturer manufacturer) throws PreexistingEntityException, Exception {
        if (manufacturer.getProductList() == null) {
            manufacturer.setProductList(new ArrayList<Product>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Product> attachedProductList = new ArrayList<Product>();
            for (Product productListProductToAttach : manufacturer.getProductList()) {
                productListProductToAttach = em.getReference(productListProductToAttach.getClass(), productListProductToAttach.getProductId());
                attachedProductList.add(productListProductToAttach);
            }
            manufacturer.setProductList(attachedProductList);
            em.persist(manufacturer);
            for (Product productListProduct : manufacturer.getProductList()) {
                Manufacturer oldManufacturerIdOfProductListProduct = productListProduct.getManufacturerId();
                productListProduct.setManufacturerId(manufacturer);
                productListProduct = em.merge(productListProduct);
                if (oldManufacturerIdOfProductListProduct != null) {
                    oldManufacturerIdOfProductListProduct.getProductList().remove(productListProduct);
                    oldManufacturerIdOfProductListProduct = em.merge(oldManufacturerIdOfProductListProduct);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findManufacturer(manufacturer.getManufacturerId()) != null) {
                throw new PreexistingEntityException("Manufacturer " + manufacturer + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Manufacturer manufacturer) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Manufacturer persistentManufacturer = em.find(Manufacturer.class, manufacturer.getManufacturerId());
            List<Product> productListOld = persistentManufacturer.getProductList();
            List<Product> productListNew = manufacturer.getProductList();
            List<String> illegalOrphanMessages = null;
            for (Product productListOldProduct : productListOld) {
                if (!productListNew.contains(productListOldProduct)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Product " + productListOldProduct + " since its manufacturerId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Product> attachedProductListNew = new ArrayList<Product>();
            for (Product productListNewProductToAttach : productListNew) {
                productListNewProductToAttach = em.getReference(productListNewProductToAttach.getClass(), productListNewProductToAttach.getProductId());
                attachedProductListNew.add(productListNewProductToAttach);
            }
            productListNew = attachedProductListNew;
            manufacturer.setProductList(productListNew);
            manufacturer = em.merge(manufacturer);
            for (Product productListNewProduct : productListNew) {
                if (!productListOld.contains(productListNewProduct)) {
                    Manufacturer oldManufacturerIdOfProductListNewProduct = productListNewProduct.getManufacturerId();
                    productListNewProduct.setManufacturerId(manufacturer);
                    productListNewProduct = em.merge(productListNewProduct);
                    if (oldManufacturerIdOfProductListNewProduct != null && !oldManufacturerIdOfProductListNewProduct.equals(manufacturer)) {
                        oldManufacturerIdOfProductListNewProduct.getProductList().remove(productListNewProduct);
                        oldManufacturerIdOfProductListNewProduct = em.merge(oldManufacturerIdOfProductListNewProduct);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = manufacturer.getManufacturerId();
                if (findManufacturer(id) == null) {
                    throw new NonexistentEntityException("The manufacturer with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Manufacturer manufacturer;
            try {
                manufacturer = em.getReference(Manufacturer.class, id);
                manufacturer.getManufacturerId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The manufacturer with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Product> productListOrphanCheck = manufacturer.getProductList();
            for (Product productListOrphanCheckProduct : productListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Manufacturer (" + manufacturer + ") cannot be destroyed since the Product " + productListOrphanCheckProduct + " in its productList field has a non-nullable manufacturerId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(manufacturer);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Manufacturer> findManufacturerEntities() {
        return findManufacturerEntities(true, -1, -1);
    }

    public List<Manufacturer> findManufacturerEntities(int maxResults, int firstResult) {
        return findManufacturerEntities(false, maxResults, firstResult);
    }

    private List<Manufacturer> findManufacturerEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Manufacturer.class));
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

    public Manufacturer findManufacturer(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Manufacturer.class, id);
        } finally {
            em.close();
        }
    }

    public int getManufacturerCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Manufacturer> rt = cq.from(Manufacturer.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
