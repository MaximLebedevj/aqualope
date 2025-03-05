## Restore a database from a dump 
```sh
$ createdb -U postgres aqualope
$ pg_restore -U postgres -d aqualope aqualope.dump
```
