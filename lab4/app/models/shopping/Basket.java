package models.shopping;

import java.util.*;
import javax.persistence.*;

import io.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import models.products.*;
import models.users.*;


// Product entity managed by Ebean
@Entity
public class Basket extends Model {

    @Id
    private Long id;
    
    @OneToMany(mappedBy = "basket", cascade = CascadeType.PERSIST)
    private List<OrderItem> basketItems;
    
    @OneToOne
    private Customer customer;

    // Default constructor
    public Basket() {

    }

    public void removeAllItems() {
        for(OrderItem i: this.basketItems) {
            i.delete();
        }
        this.basketItems = null;
    }
    public double getBasketTotal() {
        
        double total = 0;
        
        for (OrderItem i: basketItems) {
            total += i.getItemTotal();
        }
        return total;
    }
	
	//Generic query helper
    public static Finder<Long,Basket> find = new Finder<Long,Basket>(Basket.class);

    //Find all Products in the database
    public static List<Basket> findAll() {
        return Basket.find.all();
    }

    //add product to basket
    //either add a new one or update an entry
    public void addProduct(Product p) {
        boolean foundItem = false;

        //check if product is already in the basket
        //check if item is in basket
        //find order item with this product
        //if found increment quantity
        for (OrderItem i : basketItems) {
            if (i.getProduct().getId() == p.getId()) {
                i.increaseQuantity();
                itemFound = true;
                break;
            }
        }
        if (itemFound == false) {
            //add orderItem to List
            OrderItem newItem = new OrderItem(p);
            //add to items
            basketItems.add(newItems);
        }
    }

    //remove items from the basket
    public void removeItem(OrderItem item) {
        //Using an iterator ensures 'safe' removal of the list objects
        //removal of list items is unrelieable as index can change if an item is
        //added or removed elsewhere
        //iterator works with object reference which doent change
        for (Iterator<OrderItem> iter = basketItems.iterator(); iter.hasNext();) {
            if (i.getId().equals(item.getId())) {
                  //if one or more of these item in the basket the decrement
                if (i.getQuantity() > 1) {
                     i.decreaseQty();
                }
                //if only one left, remove item via iterator from the basket
                else {
                    //delete object from db
                    i.delete();
                    //remove object from list
                    iter.remove();
                    break;
                }
            }
          
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<OrderItem> getBasketItems() {
        return basketItems;
    }

    public void setBasketItems(List<OrderItem> basketItems) {
        this.basketItems = basketItems;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}