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
import com.github.olingo.demo.jpa.model.Product;
import com.github.olingo.demo.jpa.model.ProductCode;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author moscac
 */
public class ProductCodeJpaController implements Serializable {

    public ProductCodeJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(ProductCode productCode) throws PreexistingEntityException, Exception {
        if (productCode.getProductList() == null) {
            productCode.setProductList(new ArrayList<Product>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Product> attachedProductList = new ArrayList<Product>();
            for (Product productListProductToAttach : productCode.getProductList()) {
                productListProductToAttach = em.getReference(productListProductToAttach.getClass(), productListProductToAttach.getProductId());
                attachedProductList.add(productListProductToAttach);
            }
            productCode.setProductList(attachedProductList);
            em.persist(productCode);
            for (Product productListProduct : productCode.getProductList()) {
                ProductCode oldProductCodeOfProductListProduct = productListProduct.getProductCode();
                productListProduct.setProductCode(productCode);
                productListProduct = em.merge(productListProduct);
                if (oldProductCodeOfProductListProduct != null) {
                    oldProductCodeOfProductListProduct.getProductList().remove(productListProduct);
                    oldProductCodeOfProductListProduct = em.merge(oldProductCodeOfProductListProduct);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findProductCode(productCode.getProdCode()) != null) {
                throw new PreexistingEntityException("ProductCode " + productCode + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(ProductCode productCode) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            ProductCode persistentProductCode = em.find(ProductCode.class, productCode.getProdCode());
            List<Product> productListOld = persistentProductCode.getProductList();
            List<Product> productListNew = productCode.getProductList();
            List<String> illegalOrphanMessages = null;
            for (Product productListOldProduct : productListOld) {
                if (!productListNew.contains(productListOldProduct)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Product " + productListOldProduct + " since its productCode field is not nullable.");
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
            productCode.setProductList(productListNew);
            productCode = em.merge(productCode);
            for (Product productListNewProduct : productListNew) {
                if (!productListOld.contains(productListNewProduct)) {
                    ProductCode oldProductCodeOfProductListNewProduct = productListNewProduct.getProductCode();
                    productListNewProduct.setProductCode(productCode);
                    productListNewProduct = em.merge(productListNewProduct);
                    if (oldProductCodeOfProductListNewProduct != null && !oldProductCodeOfProductListNewProduct.equals(productCode)) {
                        oldProductCodeOfProductListNewProduct.getProductList().remove(productListNewProduct);
                        oldProductCodeOfProductListNewProduct = em.merge(oldProductCodeOfProductListNewProduct);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = productCode.getProdCode();
                if (findProductCode(id) == null) {
                    throw new NonexistentEntityException("The productCode with id " + id + " no longer exists.");
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
            ProductCode productCode;
            try {
                productCode = em.getReference(ProductCode.class, id);
                productCode.getProdCode();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The productCode with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Product> productListOrphanCheck = productCode.getProductList();
            for (Product productListOrphanCheckProduct : productListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This ProductCode (" + productCode + ") cannot be destroyed since the Product " + productListOrphanCheckProduct + " in its productList field has a non-nullable productCode field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(productCode);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<ProductCode> findProductCodeEntities() {
        return findProductCodeEntities(true, -1, -1);
    }

    public List<ProductCode> findProductCodeEntities(int maxResults, int firstResult) {
        return findProductCodeEntities(false, maxResults, firstResult);
    }

    private List<ProductCode> findProductCodeEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(ProductCode.class));
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

    public ProductCode findProductCode(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(ProductCode.class, id);
        } finally {
            em.close();
        }
    }

    public int getProductCodeCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<ProductCode> rt = cq.from(ProductCode.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
