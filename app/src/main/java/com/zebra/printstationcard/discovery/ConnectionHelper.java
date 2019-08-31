/***********************************************
 * CONFIDENTIAL AND PROPRIETARY
 *
 * The source code and other information contained herein is the confidential and exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published,
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 *
 * Copyright ZIH Corp. 2018
 *
 * ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.printstationcard.discovery;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.common.card.exceptions.ZebraCardException;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;

public class ConnectionHelper {
    public static final String KEY_PRINTER_ADDRESS = "ADDRESS";
    public static final String KEY_PRINTER_MODEL = "MODEL";
    public static final String KEY_PRINTER_PORT_NUMBER = "PORT_NUMBER";

    public static void cleanUpQuietly(ZebraCardPrinter zebraCardPrinter, Connection connection) {
        try {
            if (zebraCardPrinter != null) {
                zebraCardPrinter.destroy();
            }
        } catch (ZebraCardException e) {
            // Do nothing
        }

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (ConnectionException e) {
            // Do nothing
        }
    }
}
