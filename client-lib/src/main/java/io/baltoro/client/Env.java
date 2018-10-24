package io.baltoro.client;

public enum Env 
{
	PRD,
	STG,
	QA,
	DEV,
	JUNIT; // run junit API tests directly, creates test local db, after test the db is cleaned. 
}
