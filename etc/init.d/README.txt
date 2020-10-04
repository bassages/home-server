Install:
Copy file home-server to /etc/init.d and make it executable:
sudo cp home-server /etc/init.d
sudo chmod +x home-server

Execute:
sudo update-rc.d home-server defaults enable

View service status:
sudo systemctl status home-server