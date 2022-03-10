.. _how-to:

How Tos
=======

Force execution to an executor
------------------------------

Only users with admin privileges can use this override. In flow params:
set ``"useExecutor" = EXECUTOR_ID``.

Setting flow priority in multiple executor mode
-----------------------------------------------

Only users with admin privileges can use this property. In flow params:
set ``"flowPriority" = PRIORITY``. Higher numbers get executed first.

Enabling and Disabling Queue in multiple executor mode
------------------------------------------------------

Only users with admin privileges can use this action. Use curl or simply
visit following URL:-

-  Enable: ``WEBSERVER_URL/executor?ajax=disableQueueProcessor``
-  Disable: ``WEBSERVER_URL/executor?ajax=enableQueueProcessor``

Reloading executors in multiple executor mode
---------------------------------------------

Only users with admin privileges can use this action. This action need
at least one active executor to be successful. Use curl or simply visit
following URL:- ``WEBSERVER_URL/executor?ajax=reloadExecutors``