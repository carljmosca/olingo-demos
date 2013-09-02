package com.github.olingo.demo.jpa;

import com.github.olingo.demo.jpa.model.Customer;
import com.github.olingo.demo.jpa.model.DiscountCode;
import com.github.olingo.demo.jpa.model.MicroMarket;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class App {

    private static final String PERSISTENCE_UNIT_NAME = "sampleDb";
    private static EntityManagerFactory factory;

    public static void main(String[] args) {
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        EntityManager em = factory.createEntityManager();

        Query q = em.createQuery("select c from Customer c");
        List<Customer> customers = q.getResultList();
        for (Customer customer : customers) {
            System.out.println("Customer: " + customer.getName() + 
                    " Email: " + customer.getEmail());
        }
        
        // brute force method to get a DiscountCode and MicroMarket
        // but this is a small, fast database
        q = em.createQuery("select d from DiscountCode d");
        List<DiscountCode> discountCodes = q.getResultList();
        q = em.createQuery("select q from MicroMarket q");
        List<MicroMarket> microMarkets = q.getResultList();
        if (!discountCodes.isEmpty() && !microMarkets.isEmpty()) {

            em.getTransaction().begin();
            Customer customer = new Customer();
            customer.setCustomerId(customers.size() + 1);
            customer.setName("John Smith");
            customer.setState("VA");
            customer.setZip(microMarkets.get(0));
            customer.setDiscountCode(discountCodes.get(0));
            em.persist(customer);
            em.getTransaction().commit();

            em.close();
        }
    }
}
