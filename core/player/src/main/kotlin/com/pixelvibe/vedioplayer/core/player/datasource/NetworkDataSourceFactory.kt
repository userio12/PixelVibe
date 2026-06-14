package com.pixelvibe.vedioplayer.core.player.datasource

import android.net.Uri
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.TransferListener

class NetworkDataSourceFactory : DataSource.Factory {

    override fun createDataSource(): DataSource {
        return RoutingDataSource()
    }

    private inner class RoutingDataSource : DataSource {

        private var delegate: DataSource? = null
        private var openedUri: Uri = Uri.EMPTY

        override fun open(dataSpec: DataSpec): Long {
            openedUri = dataSpec.uri
            val scheme = dataSpec.uri.scheme?.lowercase() ?: ""
            val factory: DataSource.Factory = when (scheme) {
                "smb" -> SmbDataSource.Factory()
                "ftp" -> FtpDataSource.Factory()
                "webdav" -> WebDavDataSource.Factory()
                else -> DefaultHttpDataSource.Factory()
            }
            val ds = factory.createDataSource()
            delegate = ds
            return ds.open(dataSpec)
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Long {
            return delegate?.read(buffer, offset, length) ?: -1L
        }

        override fun getUri(): Uri = delegate?.uri ?: openedUri

        override fun close() {
            delegate?.close()
            delegate = null
        }

        override fun addTransferListener(transferListener: TransferListener) {}
    }
}
