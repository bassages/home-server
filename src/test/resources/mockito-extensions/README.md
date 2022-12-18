Mockito checks the extensions directory for configuration files when it is loaded.
The file in this directory enables the mocking of final methods and classes.

Without this, when trying to mock static, Mockito throws something like:
````
Mockito cannot mock/spy because :
- final class
```