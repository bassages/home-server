1. Install:
```
sudo cp home-server.service /etc/systemd/system
```

2. Execute:
```
sudo systemctl enable home-server
```

Start service:
```
sudo systemctl start home-server
```

Stop service:
```
sudo systemctl stop home-server
```

View service status:
```
sudo systemctl status home-server
```

Reload changes made to home-server.service:
```
sudo systemctl daemon-reload
```