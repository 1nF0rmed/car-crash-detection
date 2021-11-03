import logging
from accmodule import AccModule
from datamodule import getLocation, DBMHandler, getBoxInfo
import time

logging.basicConfig(filename='app_hw_server.log', filemode='w', format='%(name)s ::: %(levelname)s ::: %(message)s', level=logging.INFO)

class HWServer:

    def __init__(self):
        logging.info("Loading the main hardware server")

        # Initialize acc module
        self.accm = AccModule()

        # Initialize database module
        self.dbh = DBMHandler()

    def start(self):

        while True:
            status,data,prev = self.accm.check_crash()
            print("SD: "+str(data))
            print("PD: "+str(prev))
            if status is True:
                # If Car crash occurs
                print("[LOG] CAR CRASH")
                self.dbh.alert_and_push([str(data), getBoxInfo()])
                logging.info("Car Crash occured")
                logging.info("Initiating alert process")
            logging.info("Sensor Data: "+str(data))
            logging.info("Previous Data: "+str(prev))
            time.sleep(1)



hws = HWServer()
hws.start()
