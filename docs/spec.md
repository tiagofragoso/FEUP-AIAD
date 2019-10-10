# Project Specification - Group 13

## Scenario

A factory produces different products and employs several machines than can build some part of a different number of products. It is assumed that all machines are connected through a production line, therefore each product can be built in any sequence of machines that fulfills its build sequence. Each product also as a different priority that must be respected when choosing witch product to build next.

> **Example:**  
> - **Product A** is built by applying the following sequence of tasks: T1 -> T2 -> T3.  
> - **Machine X** can complete tasks T1 and T3.
> - **Machine Y** can complete tasks T2 and T3.
> - **Machine Z** can complete task T3.
> 
> Product A can be built using different sequences of machines, such as:  
> 
> - X (T1) -> Y (T2, T3)
> - X (T1) -> Y (T2) -> X (T3)
> - X (T1) -> Y (T2) -> Z (T3)

## Agents

The products and the machines are the agents of the system.

## Interactions

The products interact with the machines in order to understand where they should go next, according to the task that has to be performed.

## Improvements (Nice to have)

Initially, it is to be considered that the machines have infinite resources, have uptime of 100% and perform each task correctly. In order to introduce complexity into the multi-agent system, some changes can be performed:

- Machines can run out of resources.
- Machines can be damaged and thus be unavailable for some time.
- Machines can perform some tasks incorrectly, rendering the product useless.

In order to accommodate these changes, a new agent would be introduced — the technician — which would be responsible for both refilling resources and fixing machines. This would also add interactions between the machines and the technicians.
