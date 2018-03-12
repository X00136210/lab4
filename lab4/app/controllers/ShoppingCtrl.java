 package controllers;

import play.mvc.*;
import play.data.*;
import javax.inject.Inject;

import views.html.*;
import play.db.ebean.Transactional;
import play.api.Environment;

// Import models
import models.users.*;
import models.products.*;
import models.shopping.*;

// Import security controllers
import controllers.security.*;

//Auenticate user
@Security.Authenticated(Secured.class)

//AUthorise user(check if user is a customer)
@With(CheckIfCustomer.class)

public class ShoppingCtrl extends Controller {


    /** Dependency Injection **/

    /** http://stackoverflow.com/questions/15600186/play-framework-dependency-injection **/
    private FormFactory formFactory;

    /** http://stackoverflow.com/a/37024198 **/
    private Environment env;

    /** http://stackoverflow.com/a/10159220/6322856 **/
    @Inject
    public ShoppingCtrl(Environment e, FormFactory f) {
        this.env = e;
        this.formFactory = f;
    }


    
    // Get a user - if logged in email will be set in the session
	private Customer getCurrentUser() {
		return (Customer)User.getLoggedIn(session().get("email"));
	}

    //Add item to customer basket
    @Transactional
    public Result addToBasket(Long id) {
        //find the product
        Product p = Product.find.byId(id);

        //Get basket for logged in customer
        Customer customer = (Customer)User.getLoggedIn(session().get("email"));

        //check if item is in basket
        if (customer.getBasket() == null) {
            //if no basket, create one
            customer.setBasket(new Basket());
            customer.getBasket().setCustomer(customer);
            customer.update();
        }
        //add product to basket and save
        customer.getBasket.addProduct(p);
        customer.update();

        //show the basket contents
        return ok(basket.render(customer));
    }

    //add item to the basket
    @Transactional
    public Result addOne(Long itemId) {
        //get the order item
        OrderItem item = OrderItem.find.byId(itemId);
        //increment quantity
        item.increaseQty();
        //save
        item.update();
        //show updated basket
        return redirect(routes.ShippingCtrl.showBasket());
    }

    //remove an item form the basket
    @Transactional
    public Result removeOne(Long itemId) {
        //get the order item
        OrderItem item = OrderItem.find.byId(itemId);
        //get the user
        Customer c = getCurrentUser();
        //call basket remove item method
        c.getBasket().removeItem(item);
        c.getBasket.update();
        //back to basket
        return ok(basket.reder(c));
    }

    //show basket in scala
    @Transactional
    public Result showBasket() {
        return ok(basket.render(getCurrentUser()));
    }

    // Empty Basket
    @Transactional
    public Result emptyBasket() {
        
        Customer c = getCurrentUser();
        c.getBasket().removeAllItems();
        c.getBasket().update();
        
        return ok(basket.render(c));
    }

    //place an order
    @Transactional
    public Result placeOrder() {
        Customer c = getCurrentUser();

        //create an order instance
        ShopOrder order = new shopOrder();
        
        //associate order with customer
        order.setCustomer(c);

        //copy basket to order
        order.setItems(c.getBasket.getBaketItems());

        //save the order to set new id for order
        order.save();

        //move items from basket to order
        for (OrderItem i : order.getItems()) {
            //associate from order
            i.setOrder(order);
            //remove from basket
            i.setBasket(null);
            //update item
            i.update();
        }

        //update the order
        order.update();

        //clear and update the shopping basket
        c.getBasket().setBasketItems(null);
        c.getBasket().update();

        //show order returned view
        return ok(orderConfirmed.render(c, order));
    }
    
    // View an individual order
    @Transactional
    public Result viewOrder(long id) {
        ShopOrder order = ShopOrder.find.byId(id);
        return ok(orderConfirmed.render(getCurrentUser(), order));
    }

}