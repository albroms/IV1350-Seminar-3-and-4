package se.kth.iv1350.controller;

import se.kth.iv1350.integration.ExternalSystemHandler;

import se.kth.iv1350.model.*;
/**
 * @author Alexander Broms
 * @version 1.2
 * Written 2020-05-27
 *
 * The controller class which communicates with the classes in the model and the {@link ExternalSystemHandler} that
 * handles things concerning the external systems.
 */
public class Controller {
    private Sale currentSale;
    private Register register;
    private final ExternalSystemHandler externalSystems;

    /**
     * Constructor for controller that takes a parameter for the external systems.
     * @param extSys the external system handler
     */
    public Controller(ExternalSystemHandler extSys){
        this.externalSystems = extSys;
    }

    /**
     * Create a sale for the current instance of controller.
     * @param cashierName Name of the cashier managing the sale.
     */
    public void startSale(String cashierName){
        this.currentSale = new Sale(cashierName);
    }

    /**
     * Controller receives an item from the external system handler.
     * If a valid item was returned to the controller, the controller then instructs its sale to
     * add that item to itself.
     * @param itemID item ID given
     * @param quantity quantity give
     * @return a SingleItem object to be used by the view, null if item not found.
     */
    public SingleItem scanItem(int itemID, int quantity){
        SingleItem foundItem = this.externalSystems.findItem(itemID);
        if(foundItem != null){
            this.currentSale.addItem(foundItem.getItemDTO(), quantity);
            return new SingleItem(foundItem.getItemDTO(), quantity);
        }
        return null;
    }

    /**
     * Controller tells the external system handler to produce a discount associated with
     * the provided customer ID.
     * @param customerID the provided customer ID number.
     */
    public Discount requestDiscount(int customerID){
        Discount validDiscount = this.externalSystems.findDiscount(customerID);
        if(validDiscount != null){
            this.currentSale.applyDiscount(validDiscount);
            return validDiscount;
        }
        return null;
    }

    /**
     * Controller instantiates a {@link Register} and returns the total price of the sale.
     * @return the total price of the sale.
     */
    public Amount endSale(){
        this.register = new Register(1000);
        return this.currentSale.getTotalPrice();
    }

    /**
     * Controller sends payment to register to receive change, tells the sale to prepare a receipt,
     * and tells the external system handler to finalize the sale by updating the systems.
     * @param amountPaidByCustomer the {@link Amount} given by the customer.
     * @return the change to be given to the customer, if negative, the customer is told they're short.
     */
    public Amount enterPayment(Amount amountPaidByCustomer){
        Amount change = this.register.pay(amountPaidByCustomer, this.currentSale);
        Receipt receipt = prepareReceipt(amountPaidByCustomer, change);
        finalizeSale(receipt);
        return change;
    }

    private Receipt prepareReceipt(Amount amountPaidByCustomer, Amount change){
        return currentSale.prepareReceipt(amountPaidByCustomer, change);
    }
    private void finalizeSale(Receipt receipt){
        externalSystems.finalizeSale(receipt);
    }

    //getters

    /**
     * Returns the current sale the controller has a reference to.
     * @return the current sale object
     */
    public Sale getCurrentSale(){
        return currentSale;
    }

    /**
     * Returns the {@link Register} that the controller has a reference to.
     * @return the register
     */
    public Register getRegister(){
        return register;
    }
}
