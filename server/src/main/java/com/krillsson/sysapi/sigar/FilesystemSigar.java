package com.krillsson.sysapi.sigar;

import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import com.krillsson.sysapi.domain.drive.FileSystemType;
import com.krillsson.sysapi.domain.drive.Drive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class FilesystemSigar extends SigarWrapper {

    protected FilesystemSigar(Sigar sigar) {
        super(sigar);
    }

    public List<Drive> getFilesystems() {
        org.hyperic.sigar.FileSystem[] fss = getSigarFilesystems();
        List<Drive> result = new ArrayList<>();
        for (org.hyperic.sigar.FileSystem fs : fss) {
            result.add(convertToInternal(fs));
        }
        return result;
   }

    public List<Drive> getFileSystemsWithCategory(FileSystemType fsType) {
        org.hyperic.sigar.FileSystem[] fss = getSigarFilesystems();
        return Arrays.asList(fss)
                .stream()
                .filter(f -> f.getType() == fsType.ordinal())
                .map(this::convertToInternal)
                .collect(Collectors.toList());
    }

    public Drive getFileSystemById(int id) {
        org.hyperic.sigar.FileSystem[] fss = getSigarFilesystems();
        Drive fileSystem;

        try {
            fileSystem = Arrays.asList(fss)
                    .stream()
                    .filter(f -> f.getDirName().hashCode() == id)
                    .map(this::convertToInternal)
                    .findFirst()
                    .get();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("No FS with id: '" + id + "' were found");
        }
        return fileSystem;
    }

    private Drive convertToInternal(org.hyperic.sigar.FileSystem fs) {
        try {
            FileSystemUsage usage = sigar.getFileSystemUsage(fs.getDirName());
            return SigarBeanConverter.fromSigarBean(fs, SigarBeanConverter.fromSigarBean(usage));
        } catch (SigarException e) {
            //Swallow this exception
            return SigarBeanConverter.fromSigarBean(fs, null);
        }
    }

    private org.hyperic.sigar.FileSystem[] getSigarFilesystems() {
        org.hyperic.sigar.FileSystem[] fss;

        try {
            fss = sigar.getFileSystemList();
        } catch (SigarException e) {
            // TODO: decide what message to put here
            throw new IllegalArgumentException(e.getMessage());
        }

        if (fss == null) {
            // TODO: decide what message to put here
            throw new IllegalArgumentException("Internal derp");
        }

        return fss;
    }

}
