package de.my_freaks.ts3.tsdns2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xbill.DNS.Address;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class LookupThread implements Runnable {
	private Socket sock;
	private final static Logger LOGGER = Logger.getLogger(LookupThread.class
			.getCanonicalName());

	@Override
	public void run() {
		try {
			sock.setSoTimeout(Main.getConfig().getSocketTimeout());
			byte[] inData = new byte[100];
			int readData = sock.getInputStream().read(inData);
			if (readData <= 0) {
				return;
			}
			String domain = new String(inData).trim();
			if (!Main.getConfig().isDomainAllowed(domain)) {
				return;
			}
			domain = Main.getConfig().getSrvRecordPrefix() + domain;
			SRVRecord srvRecord = calcSrvRecord(domain);
			String result;
			if (srvRecord == null) {
				result = Main.getConfig().getDefaultHost();
			} else {
				InetAddress address = Address.getByName(srvRecord.getTarget()
						.toString());
				result = address.getHostAddress() + ":" + srvRecord.getPort();
			}
			sock.getOutputStream().write(result.getBytes());
			LOGGER.log(Level.FINE, "Resolve " + domain + " to " + result);
		} catch (TextParseException e) {
			LOGGER.log(Level.WARNING, "Can't lookup domain.", e);
		} catch (SocketTimeoutException e) {
			return;
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Can't handle domain-lookup.", e);
		} finally {
			try {
				sock.close();
			} catch (IOException e) {
				// Ignore
			}
		}
	}

	private SRVRecord calcSrvRecord(String domain) throws TextParseException {
		Record[] srvRecords = new Lookup(domain, Type.SRV).run();
		if (srvRecords == null || srvRecords.length < 1) {
			return null;
		}
		int lowestPriority = ((SRVRecord) srvRecords[0]).getPriority();
		List<SRVRecord> chosenRecords = new LinkedList<SRVRecord>();
		for (int i = 0; i < srvRecords.length; ++i) {
			SRVRecord record = (SRVRecord) srvRecords[i];
			if (record.getPriority() < lowestPriority) {
				lowestPriority = record.getPriority();
				chosenRecords.clear();
			}
			if (record.getPriority() == lowestPriority) {
				chosenRecords.add(record);
			}
		}
		int recordNum = Main.getRandom().nextInt(chosenRecords.size());
		return chosenRecords.get(recordNum);
	}

	public LookupThread(Socket sock) {
		this.sock = sock;
	}
}
