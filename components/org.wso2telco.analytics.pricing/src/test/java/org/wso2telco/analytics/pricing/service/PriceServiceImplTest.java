/*
 * Copyright 2017 WSO2.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2telco.analytics.pricing.service;

import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author roshan
 */
public class PriceServiceImplTest {
    
    public PriceServiceImplTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of priceNorthBoundRequest method, of class PriceServiceImpl.
     */
    @org.junit.Test
    public void testPriceNorthBoundRequest() {
        System.out.println("priceNorthBoundRequest");
        StreamRequestData reqdata = null;
        Map.Entry<CategoryCharge, BilledCharge> categoryEntry = null;
        PriceServiceImpl instance = new PriceServiceImpl();
        instance.priceNorthBoundRequest(reqdata, categoryEntry);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of priceSouthBoundRequest method, of class PriceServiceImpl.
     */
//    @org.junit.Test
//    public void testPriceSouthBoundRequest() {
//        System.out.println("priceSouthBoundRequest");
//        StreamRequestData reqdata = null;
//        Map.Entry<CategoryCharge, BilledCharge> categoryEntry = null;
//        PriceServiceImpl instance = new PriceServiceImpl();
//        instance.priceSouthBoundRequest(reqdata, categoryEntry);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    
}
